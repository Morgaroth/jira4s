package io.gitlab.mateuszjaje.jiraclient
package models

import marshalling.Jira4sZonedDateTimeCodec

import io.circe.generic.semiauto.{deriveCodec, deriveDecoder}
import io.circe.{Codec, Decoder}

import java.time.ZonedDateTime

trait JiraPaginatedResponse[T] {
  def startAt: Int

  def maxResults: Int

  def total: Int

  def values: Vector[T]
}

case class JiraProject(key: String, name: String)

object JiraProject {
  implicit val codec: Codec[JiraProject] = deriveCodec
}

case class JiraProjects(startAt: Int, maxResults: Int, total: Int, values: Vector[JiraProject]) extends JiraPaginatedResponse[JiraProject]

case class JiraIssueShort(id: String, self: String, key: IssueKey) {
  lazy val htmlSelf: String = self.replace("rest/api/2/issue", "browse").replace(id, key.value)
}

object JiraIssueShort {
  implicit val JiraIssueShortCirceDecoder: Decoder[JiraIssueShort] = deriveDecoder[JiraIssueShort]
}

case class JiraIssueWithWorklog(id: String, self: String, key: IssueKey, fields: JiraIssueFieldsWork)

case class JiraIssueFields(
                            project: JiraProject,
                            resolution: Option[JiraResolutionObj],
                            labels: Set[String],
                            description: Option[String],
                            summary: String,
                            status: JiraStatusObj,
                            creator: JiraUser,
                            assignee: Option[JiraUser],
                            reporter: JiraUser,
                            issuetype: JiraIssueType,
                            created: ZonedDateTime,
                          )

object JiraIssueFields extends Jira4sZonedDateTimeCodec {
  implicit val JiraIssueFieldsCodec: Codec[JiraIssueFields] = deriveCodec
}

case class JiraIssue(id: String, self: String, key: IssueKey, fields: JiraIssueFields) {
  lazy val htmlSelf: String = self.replace("rest/api/2/issue", "browse").replace(id, key.value)
}

object JiraIssue {
  implicit val JiraIssueCodec: Codec[JiraIssue] = deriveCodec
}

case class JiraIssueFieldsWork(project: JiraProject, worklog: JiraWorklogs)

case class JiraPaginatedIssues(startAt: Int, maxResults: Int, total: Int, isLast: Option[Boolean], issues: Vector[JiraIssue])
  extends JiraPaginatedResponse[JiraIssue] {
  override def values: Vector[JiraIssue] = issues
}

object JiraPaginatedIssues {
  implicit val JiraPaginatedIssuesCodec: Codec[JiraPaginatedIssues] = deriveCodec
}

case class JiraResolutionObj(self: String, id: String, description: String, name: Resolution)

object JiraResolutionObj {
  implicit val codec: Codec[JiraResolutionObj] = deriveCodec
}

case class JiraStatusObj(self: String, id: String, description: String, name: String, iconUrl: String, statusCategory: JiraStatusCategory)

object JiraStatusObj {
  implicit val codec: Codec[JiraStatusObj] = deriveCodec
}

case class JiraStatusCategory(self: String, id: Long, key: String, colorName: String, name: String)

object JiraStatusCategory {
  implicit val codec: Codec[JiraStatusCategory] = deriveCodec
}

case class JiraIssueType(self: String, id: String, description: String, iconUrl: String, name: String, subtask: Boolean)

object JiraIssueType {
  implicit val codec: Codec[JiraIssueType] = deriveCodec
}

case class JiraWorklog(author: JiraUser, started: ZonedDateTime, timeSpentSeconds: Long, id: String)

case class JiraWorklogs(startAt: Int, maxResults: Int, total: Int, worklogs: Vector[JiraWorklog])
  extends JiraPaginatedResponse[JiraWorklog] {
  override def values: Vector[JiraWorklog] = worklogs
}

case class JiraUser(
                     self: String,
                     name: String,
                     key: String,
                     emailAddress: String,
                     displayName: Option[String],
                     active: Boolean,
                     timeZone: String,
                   )

object JiraUser {
  implicit val codec: Codec[JiraUser] = deriveCodec
}

case class RemoteIssueLinkIdentifies(id: Long, self: String)

object RemoteIssueLinkIdentifies {
  implicit val codec: Codec[RemoteIssueLinkIdentifies] = deriveCodec
}

case class JiraRemoteLink(
                           id: Option[Int],
                           self: String,
                           globalId: Option[String],
                           application: Option[JiraApplication],
                           relationship: Option[String],
                           `object`: RemoteLinkObject,
                         )

object JiraRemoteLink {
  implicit val codec: Codec[JiraRemoteLink] = deriveCodec
}

case class CreateJiraRemoteLink(
                                 globalId: String,
                                 application: Option[JiraApplication],
                                 relationship: Option[String],
                                 `object`: RemoteLinkObject,
                               )

object CreateJiraRemoteLink {
  implicit val codec: Codec[CreateJiraRemoteLink] = deriveCodec
}

case class JiraApplication(`type`: Option[String], name: Option[String])

object JiraApplication {
  implicit val codec: Codec[JiraApplication] = deriveCodec
}

case class RemoteLinkObject(
                             url: String,
                             title: String,
                             summary: Option[String],
                             icon: Option[Icon],
                             status: Option[JiraRemoteLinkStatus],
                           )

object RemoteLinkObject {
  implicit val codec: Codec[RemoteLinkObject] = deriveCodec
}

case class JiraRemoteLinkStatus(resolved: Option[Boolean], icon: Option[Icon])

object JiraRemoteLinkStatus {
  implicit val codec: Codec[JiraRemoteLinkStatus] = deriveCodec
}

case class Icon(
                 url16x16: Option[String],
                 title: Option[String],
                 link: Option[String],
               )

object Icon {
  implicit val codec: Codec[Icon] = deriveCodec
}

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

case class JTempoWorklog(
                          id: Long,
                          self: String,
                          issue: JTempoIssue,
                          timeSpentSeconds: Long,
                          dateStarted: ZonedDateTime,
                          project: Option[JiraProject],
                          author: JTempoUser,
                          jiraWorklogId: Option[Long],
                        )
