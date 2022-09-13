package io.gitlab.mateuszjaje.jiraclient
package apis

import models.IssueKey

import cats.data.EitherT

trait UpdateIssueAPI[F[_]] {
  self: JiraRestAPI[F] =>

  // @see: https://developer.atlassian.com/cloud/jira/platform/rest/v2/api-group-issues/#api-rest-api-2-issue-issueidorkey-put
  def updateJiraIssue(issueKey: IssueKey, updatePayload: UpdateJiraIssue): EitherT[F, JiraError, Unit] = {
    implicit val rId: RequestId = RequestId.newOne("modify-issue")
    val payload                 = ModifyJiraIssuePayload(updatePayload)
    val req                     = reqGen.put(s"$API/issue/$issueKey", MJson.write(payload))

    invokeRequest(req).map(_ => ())
  }
}
