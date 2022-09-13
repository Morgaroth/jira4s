package io.gitlab.mateuszjaje.jiraclient
package apisv2

import apisv2.ThisMonad.syntax.*
import models.IssueKey

trait UpdateIssueAPIV2[F[_]] {
  self: JiraRestAPIV2[F] =>

  // @see: https://developer.atlassian.com/cloud/jira/platform/rest/v2/api-group-issues/#api-rest-api-2-issue-issueidorkey-put
  def updateJiraIssue(issueKey: IssueKey, updatePayload: UpdateJiraIssue): F[Either[JiraError, Unit]] = {
    implicit val rId: RequestId = RequestId.newOne("modify-issue")
    val payload                 = ModifyJiraIssuePayload(updatePayload)
    val req                     = reqGen.put(s"$API/issue/$issueKey", MJson.write(payload))

    invokeRequest(req).map(_ => ())
  }
}
