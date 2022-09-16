package io.gitlab.mateuszjaje.jiraclient
package models

import marshalling.Jira4sZonedDateTimeCodec

import io.circe.*
import io.circe.generic.semiauto.{deriveCodec, deriveDecoder}

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
    customFields: Map[String, Json],
)

object JiraIssueFields extends Jira4sZonedDateTimeCodec {
  implicit val JiraIssueFieldsDecoder: Decoder[JiraIssueFields] = Decoder.instance { cursor =>
    for {
      project     <- cursor.downField("project").as[JiraProject]
      resolution  <- cursor.downField("resolution").as[Option[JiraResolutionObj]]
      labels      <- cursor.downField("labels").as[Set[String]]
      description <- cursor.downField("description").as[Option[String]]
      summary     <- cursor.downField("summary").as[String]
      status      <- cursor.downField("status").as[JiraStatusObj]
      creator     <- cursor.downField("creator").as[JiraUser]
      assignee    <- cursor.downField("assignee").as[Option[JiraUser]]
      reporter    <- cursor.downField("reporter").as[JiraUser]
      issuetype   <- cursor.downField("issuetype").as[JiraIssueType]
      created     <- cursor.downField("created").as[ZonedDateTime]
      customFields <- cursor.keys
        .getOrElse(List.empty)
        .foldLeft[Either[DecodingFailure, Map[String, Json]]](Right(Map.empty)) {
          case (Right(acc), key) if key.startsWith("customfield_") =>
            cursor.downField(key).as[Json].map { value =>
              if (value.isNull) acc
              else acc.updated(key.stripPrefix("customfield_"), value)
            }
          case (passThrough, _) => passThrough
        }
    } yield new JiraIssueFields(
      project,
      resolution,
      labels,
      description,
      summary,
      status,
      creator,
      assignee,
      reporter,
      issuetype,
      created,
      customFields,
    )
  }
  private val JiraIssueFieldsEncoderWithoutCustomFields: Encoder[JiraIssueFields] = {
    Encoder.forProduct11(
      "project",
      "resolution",
      "labels",
      "description",
      "summary",
      "status",
      "creator",
      "assignee",
      "reporter",
      "issuetype",
      "created",
    )((x: JiraIssueFields) =>
      (x.project, x.resolution, x.labels, x.description, x.summary, x.status, x.creator, x.assignee, x.reporter, x.issuetype, x.created),
    )
  }

  implicit val JiraIssueFieldsEncoder: Encoder[JiraIssueFields] = Encoder.instance[JiraIssueFields] { data =>
    val base  = JiraIssueFieldsEncoderWithoutCustomFields(data)
    val extra = Json.fromFields(data.customFields.map(x => s"customfield_${x._1}" -> x._2))
    base.deepMerge(extra)
  }
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
