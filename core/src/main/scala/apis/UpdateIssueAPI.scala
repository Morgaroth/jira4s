package io.gitlab.mateuszjaje.jiraclient
package apis

import marshalling.Jira4sMarshalling
import models.IssueKey

import cats.data.EitherT
import io.circe.Encoder
import io.circe.generic.auto._

import scala.language.postfixOps

trait UpdateIssueAPI[F[_]] extends Jira4sMarshalling {
  self: JiraRestAPI[F] =>

  // @see: https://developer.atlassian.com/cloud/jira/platform/rest/v2/api-group-issues/#api-rest-api-2-issue-issueidorkey-put
  def updateJiraIssue(issueKey: IssueKey, updatePayload: UpdateJiraIssue): EitherT[F, JiraError, Unit] = {
    implicit val rId: RequestId = RequestId.newOne("modify-issue")
    val payload                 = ModifyJiraIssuePayload(updatePayload)
    val req                     = reqGen.put(s"$API/issue/$issueKey", MJson.write(payload))

    invokeRequest(req).map(_ => ())
  }
}

case class JiraFieldUpdate(action: String, value: String)

object JiraFieldUpdate {

  implicit val JiraFieldUpdateEncoder: Encoder[JiraFieldUpdate] =
    Encoder.encodeMap[String, String].contramap[JiraFieldUpdate](x => Map(x.action -> x.value))

  def add(value: String) = JiraFieldUpdate("add", value)

  def remove(value: String) = JiraFieldUpdate("remove", value)
}

case class UpdateJiraIssue(field: String, changes: List[JiraFieldUpdate])

object UpdateJiraIssue {
  implicit val UpdateJiraIssueEncoder: Encoder[UpdateJiraIssue] =
    Encoder.encodeMap[String, List[JiraFieldUpdate]].contramap[UpdateJiraIssue](x => Map(x.field -> x.changes))
}

case class ModifyJiraIssuePayload(update: UpdateJiraIssue)
