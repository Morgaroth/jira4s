package io.morgaroth.jiraclient.createmodels

import io.circe.generic.extras.semiauto.deriveConfiguredEncoder
import io.circe.Encoder
import io.morgaroth.jiraclient.marshalling.config
import io.morgaroth.jiraclient.models.IssueKey


case class CreateJiraIssue(
                            project: JiraProjectId,
                            summary: String,
                            description: String,
                            issuetype: JiraIssueId,
                            labels: Option[Set[String]],
                            priority: Option[PriorityId],
                          )

object CreateJiraIssue {
  implicit val CreateJiraIssueCirceEncoder: Encoder[CreateJiraIssue] = deriveConfiguredEncoder[CreateJiraIssue]
}

case class CreateJiraIssuePayload(fields: CreateJiraIssue)

object CreateJiraIssuePayload {
  implicit val CreateJiraIssuePayloadCirceEncoder: Encoder[CreateJiraIssuePayload] = deriveConfiguredEncoder[CreateJiraIssuePayload]
}

case class JiraProjectId(key: Option[String], id: Option[String])

object JiraProjectId {
  implicit val JiraProjectIdCirceEncoder: Encoder[JiraProjectId] = deriveConfiguredEncoder[JiraProjectId]

  def key(value: String) = new JiraProjectId(Some(value), None)

  def id(value: String) = new JiraProjectId(None, Some(value))

}

case class JiraIssueId(id: Option[String], name: Option[String])

object JiraIssueId {
  implicit val JiraIssueIdCirceEncoder: Encoder[JiraIssueId] = deriveConfiguredEncoder[JiraIssueId]

  def id(id: String) = new JiraIssueId(Some(id), None)

  def name(value: String) = new JiraIssueId(None, Some(value))

  val bug = name("Bug")
  val task = name("Task")
}

case class PriorityId(name: Option[String])

object PriorityId {
  implicit val PriorityIdCirceEncoder: Encoder[PriorityId] = deriveConfiguredEncoder[PriorityId]

  def name(value: String) = new PriorityId(Some(value))

  val Trivial = name("Trivial")
  val Minor = name("Minor")
  val Major = name("Major")
  val Critical = name("Critical")
}

case class IssuesPayload(issues: Vector[IssueKey])

object IssuesPayload {
  implicit val IssuesPayloadCirceEncoder: Encoder[IssuesPayload] = deriveConfiguredEncoder[IssuesPayload]

}

