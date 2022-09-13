package io.gitlab.mateuszjaje.jiraclient

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

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

object ModifyJiraIssuePayload {
  implicit val UpdateJiraIssueEncoder: Encoder[ModifyJiraIssuePayload] = deriveEncoder
}
