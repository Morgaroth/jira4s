package io.gitlab.mateuszjaje.jiraclient
package apisv2

import createmodels.{CreateJiraIssue, CreateJiraIssuePayload, IssuesPayload}
import models.{IssueKey, JiraIssueShort}

trait CreateIssueAPIV2[F[_]] {
  self: JiraRestAPIV2[F] =>

  // @see: https://developer.atlassian.com/cloud/jira/platform/rest/v2/api-group-issues/#api-rest-api-2-issue-post
  // @see: https://developer.atlassian.com/server/jira/platform/jira-rest-api-examples/
  // @see: https://community.atlassian.com/t5/Answers-Developer-Questions/How-to-create-an-issue-with-the-REST-API-and-immediatly-set-the/qaq-p/515174
  def createIssue(payload: CreateJiraIssue): F[Either[JiraError, JiraIssueShort]] = {
    implicit val rId: RequestId = RequestId.newOne("create-issue")
    val payloadReq              = CreateJiraIssuePayload(payload)
    val req                     = reqGen.post(s"$API/issue", MJson.write(payloadReq))

    invokeRequest(req).unmarshall[JiraIssueShort]
  }

  def moveIssuesToEpic(epic: IssueKey, issues: Vector[IssueKey]): F[Either[JiraError, String]] = {
    implicit val rId: RequestId = RequestId.newOne("move-issues-to-epic")
    val req                     = reqGen.post(s"$API1/epic/$epic/issue", MJson.write(IssuesPayload(issues)))

    invokeRequest(req)
  }
}
