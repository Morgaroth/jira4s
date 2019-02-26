package io.morgaroth.jiraclient

import cats.data.EitherT

import scala.concurrent.Future

trait JiraClientError

case class JiraFailure(reason: String) extends JiraClientError

case class LogicError(info: String) extends JiraClientError

object DashboardMonads {
  type JiraResponse[A] = EitherT[Future, JiraClientError, A]

  implicit class StringEncodable(str: String) {
    def toUrlEncoded: String = {
      val result = List(
        "ń", "%C5%84",
        "ć", "%C4%87",
        "ą", "%C4%85"
      ).grouped(2).foldLeft(str) {
        case (value, what :: to :: Nil) => {
          value.replace(what, to)
        }
      }
      result
    }
  }

}