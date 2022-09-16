package io.gitlab.mateuszjaje.jiraclient
package query
package syntax

import query.jql.syntax.JqlQEntry

import java.net.URLEncoder

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
  lazy val render: String = s"$name=${URLEncoder.encode(value, "utf-8")}"
}

case class KVParam(name: String, value: String) extends JParam(name, value)

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

  object Post extends Method

  object Put extends Method

  object Delete extends Method

}

case class JiraRequest(
    service: String,
    authToken: String,
    method: Method,
    path: String,
    query: Vector[JiraQuery],
    payload: Option[String],
) {
  lazy val render: String = {
    val base = s"$service/$path"
    if (query.nonEmpty) {
      s"$base?${query.map(_.render).mkString("&")}"
    } else base
  }
}

case class RequestGenerator(cfg: JiraConfig) {
  def get(path: String): JiraRequest =
    JiraRequest(cfg.address, cfg.getAuthHeaderValue, Methods.Get, path, Vector.empty, None)

  def get(path: String, query: JiraQuery*): JiraRequest =
    JiraRequest(cfg.address, cfg.getAuthHeaderValue, Methods.Get, path, query.toVector, None)

  def post(path: String, data: String): JiraRequest =
    JiraRequest(cfg.address, cfg.getAuthHeaderValue, Methods.Post, path, Vector.empty, Some(data))

  def put(path: String, data: String): JiraRequest =
    JiraRequest(cfg.address, cfg.getAuthHeaderValue, Methods.Put, path, Vector.empty, Some(data))

  def delete(path: String, query: JiraQuery*): JiraRequest =
    JiraRequest(cfg.address, cfg.getAuthHeaderValue, Methods.Delete, path, query.toVector, None)
}
