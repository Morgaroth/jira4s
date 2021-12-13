package io.gitlab.mateuszjaje.jiraclient
package createmodels

import models.IssueKey

import io.circe.generic.semiauto.deriveEncoder
import io.circe.{Encoder, Json}

case class CreateJiraIssue(
    project: JiraProjectId,
    summary: String,
    description: String,
    issuetype: JiraIssueId,
    labels: Option[Set[String]],
    priority: Option[PriorityId],
    customFields: Map[String, String],
)

object CreateJiraIssue {

  private val simpleEncoder =
    Encoder.forProduct6("project", "summary", "description", "issuetype", "labels", "priority")((e: CreateJiraIssue) =>
      (e.project, e.summary, e.description, e.issuetype, e.labels, e.priority),
    )

  implicit val CreateJiraIssueCirceEncoder: Encoder[CreateJiraIssue] = { (a: CreateJiraIssue) =>
    val obj = a.customFields.foldLeft(simpleEncoder.encodeObject(a)) { case (acc, (k, v)) => acc.add(k, Json.fromString(v)) }
    Json.fromJsonObject(obj)
  }
}

case class CreateJiraIssuePayload(fields: CreateJiraIssue)

object CreateJiraIssuePayload {
  implicit val CreateJiraIssuePayloadCirceEncoder: Encoder[CreateJiraIssuePayload] = deriveEncoder[CreateJiraIssuePayload]
}

case class JiraProjectId(key: Option[String], id: Option[String])

object JiraProjectId {
  implicit val JiraProjectIdCirceEncoder: Encoder[JiraProjectId] = deriveEncoder[JiraProjectId]

  def key(value: String) = new JiraProjectId(Some(value), None)

  def id(value: String) = new JiraProjectId(None, Some(value))

}

case class JiraIssueId(id: Option[String], name: Option[String])

object JiraIssueId {
  implicit val JiraIssueIdCirceEncoder: Encoder[JiraIssueId] = deriveEncoder[JiraIssueId]

  def id(id: String) = new JiraIssueId(Some(id), None)

  def name(value: String) = new JiraIssueId(None, Some(value))

  val bug  = name("Bug")
  val task = name("Task")
}

case class PriorityId(name: Option[String])

object PriorityId {
  implicit val PriorityIdCirceEncoder: Encoder[PriorityId] = deriveEncoder[PriorityId]

  def name(value: String) = new PriorityId(Some(value))

  val Trivial  = name("Trivial")
  val Minor    = name("Minor")
  val Major    = name("Major")
  val Critical = name("Critical")
}

case class IssuesPayload(issues: Vector[IssueKey])

object IssuesPayload {
  implicit val IssuesPayloadCirceEncoder: Encoder[IssuesPayload] = deriveEncoder[IssuesPayload]
}
