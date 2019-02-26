package io.morgaroth.jiraclient

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

case class JiraIssues(startAt: Int, maxResults: Int, total: Int, issues: Vector[JiraIssue]) extends JiraPaginatedResponse[JiraIssue] {
  override def values: Vector[JiraIssue] = issues
}

case class JiraWorklog(author: JiraUser, started: DateTime, timeSpentSeconds: Long, id: String)

case class JiraWorklogs(startAt: Int, maxResults: Int, total: Int, worklogs: Vector[JiraWorklog]) extends JiraPaginatedResponse[JiraWorklog] {
  override def values: Vector[JiraWorklog] = worklogs
}

case class JiraUser(displayName: Option[String], name: String, emailAddress: String)


// Tempo models

case class JTempoIssueType(name: String, iconUrl: String)

case class JTempoIssue(key: String, id: Long, summary: String, issueType: JTempoIssueType, projectId: Long)

case class JTempoUser(displayName: String, avatar: String, self: String, key: String)

case class JTempoWorklog(id: Long, self: String, issue: JTempoIssue, timeSpentSeconds: Long, dateStarted: DateTime, project: Option[JiraProject], author: JTempoUser, jiraWorklogId: Option[Long])