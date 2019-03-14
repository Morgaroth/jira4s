package io.morgaroth.jiraclient.models

import org.joda.time.DateTime

trait JiraPaginatedResponse[T] {
  def startAt: Int

  def maxResults: Int

  def total: Int

  def values: Vector[T]
}

case class JiraProject(key: String, name: String)

case class JiraProjects(startAt: Int, maxResults: Int, total: Int, values: Vector[JiraProject]) extends JiraPaginatedResponse[JiraProject]

case class JiraIssue(id: String, self: String, key: String, fields: JiraIssueFields)

case class JiraIssueWithWorklog(id: String, self: String, key: String, fields: JiraIssueFieldsWork)

case class JiraIssueFields(project: JiraProject)

case class JiraIssueFieldsWork(project: JiraProject, worklog: JiraWorklogs)

case class JiraPaginatedIssues(startAt: Int, maxResults: Int, total: Int, isLast: Option[Boolean], issues: Vector[JiraIssue]) extends JiraPaginatedResponse[JiraIssue] {
  override def values: Vector[JiraIssue] = issues
}

case class JiraWorklog(author: JiraUser, started: DateTime, timeSpentSeconds: Long, id: String)

case class JiraWorklogs(startAt: Int, maxResults: Int, total: Int, worklogs: Vector[JiraWorklog]) extends JiraPaginatedResponse[JiraWorklog] {
  override def values: Vector[JiraWorklog] = worklogs
}

case class JiraUser(displayName: Option[String], name: String, emailAddress: String)

case class RemoteIssueLinkIdentifies(id: Long, self: String)

case class JiraRemoteLink(
                           id: Option[Int],
                           self: String,
                           globalId: Option[String],
                           application: Option[JiraApplication],
                           relationship: Option[String],
                           `object`: RemoteLinkObject
                         )

case class CreateJiraRemoteLink(
                                 globalId: String,
                                 application: Option[JiraApplication],
                                 relationship: Option[String],
                                 `object`: RemoteLinkObject
                               )

case class JiraApplication(`type`: Option[String], name: Option[String])

case class RemoteLinkObject(
                             url: String,
                             title: String,
                             summary: Option[String],
                             icon: Option[Icon],
                             status: Option[JiraRemoteLinkStatus]
                           )

case class JiraRemoteLinkStatus(resolved: Option[Boolean], icon: Option[Icon])

case class Icon(
                 url16x16: Option[String],
                 title: Option[String],
                 link: Option[String],
               )

trait Relationship {
  def raw: String
}

case object LinksTo extends Relationship {
  val raw = "links-to"
}

// Tempo models

case class JTempoIssueType(name: String, iconUrl: String)

case class JTempoIssue(key: String, id: Long, summary: String, issueType: JTempoIssueType, projectId: Long)

case class JTempoUser(displayName: String, avatar: String, self: String, key: String)

case class JTempoWorklog(id: Long, self: String, issue: JTempoIssue, timeSpentSeconds: Long, dateStarted: DateTime, project: Option[JiraProject], author: JTempoUser, jiraWorklogId: Option[Long])