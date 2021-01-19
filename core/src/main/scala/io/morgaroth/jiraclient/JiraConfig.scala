package io.morgaroth.jiraclient

import com.typesafe.config.Config

import java.util.Base64

case class JiraConfig(address: String, login: String, pass: String) {
  assert(login.nonEmpty && pass.nonEmpty, "Jira credentials empty!")

  def getBasicAuthHeaderValue = s"Basic ${Base64.getEncoder.encodeToString(s"$login:$pass".getBytes("utf-8"))}"
}

object JiraConfig {
  def fromConfig(config: Config) = new JiraConfig(
    config.getString("address"),
    config.getString("login"),
    config.getString("password"),
  )
}
