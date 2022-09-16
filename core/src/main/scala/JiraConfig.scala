package io.gitlab.mateuszjaje.jiraclient

import com.typesafe.config.Config

import java.util.Base64

trait AuthMechanism extends Product with Serializable {
  def authHeader: String
}
case class Basic(login: String, password: String) extends AuthMechanism {
  assert(login.nonEmpty && password.nonEmpty, "Jira credentials empty!")
  override val authHeader = s"Basic ${Base64.getEncoder.encodeToString(s"$login:$password".getBytes("utf-8"))}"
}
case class AccessToken(token: String) extends AuthMechanism {
  override val authHeader = s"Bearer $token"
}

case class JiraConfig(address: String, auth: AuthMechanism) {
  val getAuthHeaderValue = auth.authHeader
}

object JiraConfig {
  def fromConfig(config: Config) = {
    val authMechanism = if (config.hasPath("auth")) {
      val authConfig = config.getConfig("auth")
      authConfig.getString("type") match {
        case "basic"        => Basic(authConfig.getString("login"), authConfig.getString("password"))
        case "access-token" => AccessToken(authConfig.getString("access-token"))
      }
    } else {
      Basic(config.getString("login"), config.getString("password"))
    }
    new JiraConfig(
      config.getString("address"),
      authMechanism,
    )

  }
}
