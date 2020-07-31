package io.morgaroth.jiraclient.createmodels

import io.morgaroth.jiraclient.models.IssueKey


case class CreateJiraIssue(
                            project: JiraProjectId,
                            summary: String,
                            description: String,
                            issuetype: JiraIssueId,
                            labels: Option[Set[String]],
                            priority: Option[PriorityId],
                          )

case class CreateJiraIssuePayload(fields: CreateJiraIssue)

case class JiraProjectId(key: Option[String], id: Option[String])

object JiraProjectId {
  def key(value: String) = new JiraProjectId(Some(value), None)

  def id(value: String) = new JiraProjectId(None, Some(value))
}

case class JiraIssueId(id: Option[String], name: Option[String])

object JiraIssueId {
  def id(id: String) = new JiraIssueId(Some(id), None)

  def name(value: String) = new JiraIssueId(None, Some(value))

  val bug = name("Bug")
  val task = name("Task")
}

case class PriorityId(name: Option[String])

object PriorityId {
  def name(value: String) = new PriorityId(Some(value))

  val Trivial = name("Trivial")
  val Minor = name("Minor")
  val Major = name("Major")
  val Critical = name("Critical")
}

case class IssuesPayload(issues: Vector[IssueKey])

