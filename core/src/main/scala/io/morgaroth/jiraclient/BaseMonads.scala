package io.morgaroth.jiraclient

trait JiraError

case class RequestingError(description: String, cause: Throwable) extends JiraError

case class HttpError(statusCode: Int, description: String, errorBody: Option[String]) extends JiraError

case class MarshallingError(description: String, cause: Throwable) extends JiraError

case class UnmarshallingError(description: String, cause: Throwable) extends JiraError

case class InvalidQueryError(description: String, cause: Throwable) extends JiraError

object JiraMonads {

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