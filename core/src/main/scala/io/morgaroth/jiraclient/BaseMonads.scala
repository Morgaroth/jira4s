package io.morgaroth.jiraclient

trait JiraError

case class RequestingError(description: String, requestId: String, cause: Throwable) extends JiraError

case class HttpError(statusCode: Int, description: String, requestId: String, requestType: String, errorBody: Option[String])
    extends JiraError

case class MarshallingError(description: String, requestId: String, cause: Throwable) extends JiraError

case class UnmarshallingError(description: String, requestId: String, cause: Throwable) extends JiraError

case class InvalidQueryError(description: String, requestId: String, cause: Throwable) extends JiraError

object JiraMonads {

  implicit class StringEncodable(str: String) {
    def toUrlEncoded: String = {
      val result = List(
        "ń",
        "%C5%84",
        "ć",
        "%C4%87",
        "ą",
        "%C4%85",
      ).grouped(2).foldLeft(str) {
        case (value, what :: to :: Nil) =>
          value.replace(what, to)
        case _ => ???
      }
      result
    }
  }

}
