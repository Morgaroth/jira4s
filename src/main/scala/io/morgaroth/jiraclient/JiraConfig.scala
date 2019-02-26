package io.morgaroth.jiraclient

import java.util.Base64

case class JiraConfig(address: String, login: String, pass: String, jiraId: String) {
  assert(login.nonEmpty && pass.nonEmpty, "Jira credentials empty!")

  def getBasicAuthHeaderValue = s"Basic ${Base64.getEncoder.encodeToString(s"$login:$pass".getBytes("utf-8"))}"
}
