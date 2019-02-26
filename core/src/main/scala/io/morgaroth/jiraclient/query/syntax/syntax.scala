package io.morgaroth.jiraclient.query.syntax

import io.morgaroth.jiraclient.JiraConfig
import io.morgaroth.jiraclient.query.jql.syntax.JqlQEntry

import scala.language.implicitConversions

sealed trait JiraQuery {
  def render: String
}

object JiraQuery {
  implicit def raw(raw: String): RawQuery = RawQuery(raw)
}

case class RawQuery(raw: String) extends JiraQuery {
  lazy val render: String = raw
}

case class JFields(fields: List[String]) extends JiraQuery {
  lazy val render: String = s"fields=${fields.mkString(",")}"
}

object JFields {
  def apply(raw: String) = new JFields(raw.split(",").toList)
}

class JParam(name: String, value: String) extends JiraQuery {
  lazy val render: String = s"$name=$value"
}

case class Jql(query: JqlQEntry) extends JParam("jql", query.render)

case class JPage(startAt: Int, maxResults: Int) extends JiraQuery {
  lazy val render: String = s"startAt=$startAt&maxResults=$maxResults"
}

case class JTUser(login: String) extends JParam("username", login)

case class JTFrom(fromDate: String) extends JParam("dateFrom", fromDate)

case class JTTo(toDate: String) extends JParam("dateTo", toDate)

trait Method

object Methods {

  object Get extends Method

}

case class JiraRequest(
                        service: String,
                        authToken: String,
                        method: Method,
                        path: String,
                        query: List[JiraQuery]
                      ) {
  lazy val render: String = {
    val base = s"$service/$path"
    if (query.nonEmpty) {
      s"$base?${query.map(_.render).mkString("&")}"
    } else base
  }
}

object JiraRequest {
  def forServer(cfg: JiraConfig): (Method, String, List[JiraQuery]) => JiraRequest = new JiraRequest(cfg.address, cfg.getBasicAuthHeaderValue, _, _, _)
}