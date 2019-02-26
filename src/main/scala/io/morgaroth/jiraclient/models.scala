package io.morgaroth.jiraclient

import org.joda.time.DateTime

case class JiraPaginatedProjects(startAt: Int, maxResults: Int, total: Int, values: List[JProject])

case class JProject(key: String, name: String)

case class FieldsWork(project: JProject, worklog: JiraPaginatedWorklog)

case class JIssue(id: String, self: String, key: String, fields: FieldsWork)

case class JiraPaginatedIssuesWork(startAt: Int, maxResults: Int, total: Int, issues: Vector[JIssue])

case class JWorklog(author: JUser, started: DateTime, timeSpentSeconds: Long, id: String)

case class JiraPaginatedWorklog(startAt: Int, maxResults: Int, total: Int, worklogs: List[JWorklog])

case class JUser(displayName: Option[String], name: String, emailAddress: String)

// Tempo models

case class JTempoIssueType(name: String, iconUrl: String)

case class JTempoIssue(key: String, id: Long, summary: String, issueType: JTempoIssueType, projectId: Long)

case class JTempoUser(displayName: String, avatar: String, self: String, key: String)

case class JTempoWorklog(id: Long, self: String, issue: JTempoIssue, timeSpentSeconds: Long, dateStarted: DateTime, project: Option[JProject], author: JTempoUser, jiraWorklogId: Option[Long])