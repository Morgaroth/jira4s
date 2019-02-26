package com.williamhill.jirareports

import java.io.PrintWriter

import cats.implicits._
import com.typesafe.scalalogging.LazyLogging
import com.williamhill.jirareports.app.ServerConfig
import com.williamhill.jirareports.jira.DashboardMonads._
import com.williamhill.jirareports.jira.{JiraRequestsNew, UserNotFound}
import com.williamhill.jirareports.models.{JIssue, JProject}
import com.williamhill.jirareports.storage.{UserDao, UserInfoDao, WorklogDao}
import io.morgaroth.jiraclient.JiraConfig
import org.joda.time.DateTime

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.io.Source


class UsersService(implicit
                   cfg: ServerConfig,
                   req: JiraRequestsNew,
                   userDb: UserDao,
                   userInfoDb: UserInfoDao,
                   worklogDb: WorklogDao
                  ) extends LazyLogging {

  val BATCH_SIZE = 100

  private def fetchUserWorklogsFromRawAPI(username: String, newerThan: DateTime, fetched: Int = 0, from: Int = 0)(implicit jiraCfg: JiraConfig): JiraResponse[Int] = {
    req.fetchIssuesWithWorklogsOfUser(username, from, BATCH_SIZE, newerThan).flatMap { issues =>
      val saveWorklogsOfFetchedIssues = issues.traverse { jIssue =>
        worklogDb.save(jIssue)
        if (jIssue.fields.worklog.worklogs.lengthCompare(20) < 0) 0.pure[JiraResponse] else handleIssueWorklogs(username, jIssue)
      }
      saveWorklogsOfFetchedIssues.flatMap { _ =>
        if (issues.length == BATCH_SIZE)
          fetchUserWorklogsFromRawAPI(username, newerThan, BATCH_SIZE + fetched, from + BATCH_SIZE)
        else (fetched + issues.length).pure[JiraResponse]
      }
    }
  }

  private def fetchUserWorklogsFromTempoAPI(username: String, year: DateTime)(implicit jiraCfg: JiraConfig): JiraResponse[Int] = {
    val `1st of Jan` = year.withDayOfYear(1).withTimeAtStartOfDay()
    val `31st of Dec` = year.plusYears(1).withDayOfYear(1).withTimeAtStartOfDay()

    req.fetchWorklogsOfUserFromTempoAPI(username, `1st of Jan`, `31st of Dec`).flatMap { worklogs =>
      logger.info(s"Fetched ${worklogs.size} worklogs for user $username from tempo API")
      req.fetchIssuesByKeys(worklogs.map(_.issue.key).distinct).map(_.map(x => x.key -> x.fields.project).toMap).map { projectsByIssueKey =>
        worklogs.map { jtWorklog =>
          val userMaybe = userDb.findByLoginAndJira(jtWorklog.author.key)
          val proj: JProject = projectsByIssueKey(jtWorklog.issue.key)
          userMaybe.map { user =>
            worklogDb.save(jtWorklog.copy(project = Some(proj)), user)
            1
          }.getOrElse {
            logger.warn(s"No user key:${jtWorklog.author.key}__${jiraCfg.jiraId} for jtworklog $jtWorklog")
            0
          }
        }.sum
      }
    }
  }

  private def handleIssueWorklogs(user: String, i: JIssue, fetched: Int = 0, from: Int = 0)(implicit jiraCfg: JiraConfig): JiraResponse[Int] = {
    req.fetchWorklogsOfIssue(i, user, from, BATCH_SIZE).flatMap { worklogs =>
      userDb.save(worklogs.map(_.author).distinct.toList)
      worklogDb.save(i, worklogs)
      if (worklogs.length == BATCH_SIZE)
        handleIssueWorklogs(user, i, BATCH_SIZE + fetched, from + BATCH_SIZE)
      else (fetched + worklogs.length).pure[JiraResponse]
    }
  }

  def getUserName(email: String)(implicit jiraCfg: JiraConfig): JiraResponse[String] = {
    userDb.findByEmailAndJira(email).map(_.userKey.pure[JiraResponse]).getOrElse {
      req.getUserByEmail(email).map { newUser =>
        userDb.save(List(newUser))
        newUser.name
      }
    }
  }

  def fetchUserWorklogsByUsername(year: DateTime)(u: String)(implicit jiraConfig: JiraConfig) = {
    if (jiraConfig.jiraId == "WH") fetchUserWorklogsFromRawAPI(u, year.withDayOfYear(1).withTimeAtStartOfDay()) else fetchUserWorklogsFromTempoAPI(u, year)
  }

  def fetchUserFromUsername(u: String, year: DateTime)(implicit jiraCfg: JiraConfig): Unit = {
    val allWork = fetchUserWorklogsByUsername(year)(u)
    val result = allWork.value in 1.hour
    println(result)
  }

  def fetchWorklogsOfUsers(users: Vector[String], year: DateTime)(implicit jiraCfg: JiraConfig): Unit = {
    val work: String => JiraResponse[Int] = getUserName(_).flatMap(fetchUserWorklogsByUsername(year))
    val allWork = users.foldLeft(0.pure[JiraResponse]) {
      case (acc, us) => acc.flatMap { a =>
        work(us).map {
          i => i + a
        }.recoverWith {
          case UserNotFound(e) => acc
        }
      }
    }

    val result = allWork.value in 1.hour
    println(result)
  }


  def fetchIssuesOfUsersFromFile(path: String, year: DateTime)(implicit jCfg: JiraConfig): Unit = {
    logger.info(s"pwd is ${System.getProperty("user.dir")}")
    val users = Source.fromFile(path).getLines().toVector
    fetchWorklogsOfUsers(users, year)
  }


  def fetchUsersForEmailsFromFile(path: String)(implicit jCfg: JiraConfig): Unit = {
    val emails = Source.fromFile(path).getLines().toVector
    emails.foreach { email =>
      req.getUserByEmail(email).map { newUser =>
        userInfoDb.updateInfo(newUser)
      }.value in 5.minutes
    }
    logger.info("Users fetched.")
  }

  def printInfoStats(path: String): PrintWriter = {
    val emails = Source.fromFile(path).getLines().toVector
    val data = emails.map { email =>
      val s = userInfoDb.findByEmail(email).map(_.foundIn).getOrElse(Vector.empty).map(_.jiraId)
      s.sorted -> email
    }.groupBy(_._1).mapValues(_.map(_._2).sorted)
      .toList.sortBy(_._1.length).reverse

    new PrintWriter("stats.txt") {
      data.foreach {
        case (emptyVec, users) if emptyVec.isEmpty =>
          write("Users not found at any jira server (")
          write(String.valueOf(users.length))
          write("):\n")
          users.foreach { u =>
            write("\t")
            write(u)
            write("\n")
          }
        case (foundIn, users) =>
          write("Users in ")
          write(foundIn.mkString(", "))
          write(" (")
          write(String.valueOf(users.length))
          write("):\n")
          users.foreach { u =>
            write("\t")
            write(u)
            write("\n")
          }
      }
      close()
    }
  }
}
