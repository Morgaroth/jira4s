//package com.williamhill.jirareports.jira
//
//import akka.actor.ActorSystem
//import akka.http.scaladsl.model.HttpRequest
//import akka.http.scaladsl.model.headers.RawHeader
//import akka.stream.Materializer
//import akka.util.Timeout
//import cats.data.EitherT
//import cats._
//import cats.implicits._
//import com.typesafe.scalalogging.LazyLogging
//import com.williamhill.jirareports._
//import com.williamhill.jirareports.jira.DashboardMonads._
//import com.williamhill.jirareports.jira.JqlQEntry._
//import com.williamhill.jirareports.models._
//import com.williamhill.jirareports.utilities.HttpUtils
//import io.circe.Decoder
//import io.circe.generic.auto._
//import io.morgaroth.jiraclient.DashboardMonads.JiraResponse
//import io.morgaroth.jiraclient._
//import org.joda.time.DateTime
//
//import scala.concurrent.Future
//import scala.concurrent.duration._
//import scala.language.postfixOps
//
//class JiraRequestsNew(implicit as: ActorSystem, mat: Materializer)
//  extends HttpUtils with ExecContext with LazyLogging {
//
//  implicit val tm: Timeout = 60 seconds
//
//  val API = "rest/api/2"
//  val TEMPO = "rest/tempo-timesheets/3"
//
//  private def doJiraRequest[T](name: String, api: String, path: String, query: JiraQuery*)(implicit encoder: Decoder[T], jCfg: JiraConfig): JiraResponse[T] = {
//    val req = JiraRequest(jCfg.address, api + path, query.toList)
//    logger.info(s"Jira request [$name]: ${req.render}")
//    val response = EitherT(doRequest[T](HttpRequest(uri = req.render).withHeaders(List(RawHeader("Authorization", jCfg.getBasicAuthHeaderValue))), name)) leftMap {
//      failure => JiraFailure(failure.toString)
//    }
//    JiraResponse(response.value)
//  }
//
//  def getAllProjects(implicit jCfg: JiraConfig): JiraResponse[List[JProject]] = doJiraRequest[List[JProject]]("all-projects", API, "/project")
//
//  def getUserByEmail(email: String)(implicit jCfg: JiraConfig): JiraResponse[JUser] = {
//    doJiraRequest[List[JUser]]("get-user", API, s"/user/search/?username=$email").toDashboard.subflatMap {
//      posisble =>
//        if (posisble.lengthCompare(1) > 1) {
//          logger.warn(s"Multiple users returned for $email")
//        } else if (posisble.lengthCompare(0) == 0) {
//          logger.error(s"no user for $email at ${jCfg.jiraId}")
//        }
//        posisble.headOption.map(_.asRight[JiraClientError]).getOrElse(UserNotFound(email).asInstanceOf[JiraClientError].asLeft[JUser])
//    }
//  }
//
//
//  def fetchWorklogsOfIssue(i: JIssue, user: String, startingFrom: Int, max: Int)(implicit jCfg: JiraConfig): JiraResponse[Vector[JWorklog]] = {
//
//
//    def getPage(start: Int) = {
//      doJiraRequest[JiraPaginatedWorklog](s"issue-worklogs-$user", API, s"/issue/${i.id}/worklog",
//        JPage(start, 50)
//      ).toDashboard
//    }
//
//    def getAll(startFrom: Int, acc: Vector[JWorklog]): JiraResponse[Vector[JWorklog]] = {
//      getPage(startFrom).flatMap { resp =>
//        val currentIssues = acc ++ resp.worklogs
//        val currentCnt = currentIssues.size
//        if (currentCnt >= resp.total || currentCnt >= max || resp.worklogs.isEmpty || (resp.startAt < startFrom && resp.total == resp.maxResults && resp.total == resp.worklogs.size)) {
//          currentIssues.pure[JiraResponse]
//        } else {
//          getAll(startFrom + 50, currentIssues)
//        }
//      }
//    }
//
//    getAll(startingFrom, Vector.empty)
//  }
//
//  def fetchIssuesWithWorklogsOfUser(username: String, startingFrom: Int, max: Int, newerThan:DateTime)(implicit jCfg: JiraConfig): JiraResponse[Vector[JIssue]] = {
//    def getPage(start: Int) = {
//      doJiraRequest[JiraPaginatedIssuesWork]("user-issues", API, "/search",
//        JPage(start, 50),
//        JFields("project,worklog"),
//        Jql("worklogAuthor" === username.toUrlEncoded and "worklogDate".gte(newerThan.toString("yyyy-MM-dd")))
//      ).toDashboard
//    }
//
//    def getAll(startFrom: Int, acc: Vector[JIssue]): JiraResponse[Vector[JIssue]] = {
//      getPage(startFrom).flatMap { resp =>
//        val currentIssues = acc ++ resp.issues
//        val currentCnt = currentIssues.size
//        if (currentCnt > resp.total || currentCnt >= max || resp.issues.isEmpty) {
//          currentIssues.pure[JiraResponse]
//        } else {
//          getAll(startFrom + 50, currentIssues)
//        }
//      }
//    }
//
//    getAll(startingFrom, Vector.empty)
//  }
//
//  def fetchIssuesByKeys(keys: Vector[String])(implicit jCfg: JiraConfig): JiraResponse[Vector[JIssue]] = {
//    def getPage(keysToFetch: Vector[String]): JiraResponse[JiraPaginatedIssuesWork] = {
//      doJiraRequest[JiraPaginatedIssuesWork]("issues-by-keys", API, "/search",
//        JPage(0, 50),
//        JFields("project,worklog"),
//        Jql("id" in keysToFetch.toSet)
//      ).toDashboard.map { resp =>
//        if (resp.issues.length != keysToFetch.size) logger.error(s"DIFFERENT RESULT THAN REQUESTED ${keysToFetch.size} ${resp.issues.size}")
//        resp
//      }
//
//    }
//
//    if (keys.isEmpty) Vector.empty[JIssue].pure[JiraResponse]
//    else keys.distinct.grouped(50).toVector.traverse(getPage).map(_.flatMap(_.issues))
//  }
//
//  def fetchWorklogsOfUserFromTempoAPI(username: String, from: DateTime, to: DateTime)(implicit jCfg: JiraConfig): JiraResponse[Vector[JTempoWorklog]] = {
//    doJiraRequest[Vector[JTempoWorklog]](s"tempo-worklogs-$username", TEMPO, "/worklogs/",
//      JTUser(username), JTFrom(from.toString("yyyy-MM-dd")), JTTo(to.toString("yyyy-MM-dd"))
//    ).toDashboard
//  }
//}