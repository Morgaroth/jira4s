package io.morgaroth.jiraclient.sttpbackend

import cats.Monad
import cats.data.EitherT
import cats.syntax.either._
import com.softwaremill.sttp._
import io.circe.generic.auto._
import io.morgaroth.jiraclient.ProjectsJsonFormats.MJson
import io.morgaroth.jiraclient._
import io.morgaroth.jiraclient.query.syntax.JiraRequest

import scala.concurrent.Future
import scala.util.Try

class SttpJiraAPI(val config: JiraConfig)(implicit val m: Monad[Future]) extends JiraRestAPI[Future, String] {

  implicit val backend: SttpBackend[Try, Nothing] = TryHttpURLConnectionBackend()

  override def invokeRequest(requestData: JiraRequest): EitherT[Future, JiraError, String] = {
    val u = requestData.render
    val request = sttp.get(uri"$u").headers("Authorization" -> requestData.authToken)

    val response = request
      .send()
      .toEither.leftMap[JiraError](RequestingError("try-http-backend-left", _))
      .flatMap {
        response => response.rawErrorBody.leftMap(error => HttpError(response.code.intValue(), "http-response-error", Some(error)))
      }

    EitherT.fromEither(response)
  }

  override protected def deserializeListAllProjects(in: String): EitherT[Future, JiraError, Vector[JiraProject]] = {
    EitherT.fromEither(
      MJson.read[Vector[JiraProject]](in).leftMap[JiraError](e => UnmarshallingError(e.getMessage, e))
    )
  }

  override protected def deserializeJiraIssue(in: String): EitherT[Future, JiraError, JiraIssue] = {
    EitherT.fromEither(
      MJson.read[JiraIssue](in).leftMap[JiraError](e => UnmarshallingError(e.getMessage, e))
    )
  }

  override protected def deserializePaginatedProjects(in: String): EitherT[Future, JiraError, JiraProjects] = {
    EitherT.fromEither(
      MJson.read[JiraProjects](in).leftMap[JiraError](e => UnmarshallingError(e.getMessage, e))
    )
  }
}