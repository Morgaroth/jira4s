package io.morgaroth.jiraclient.sttpbackend

import cats.Monad
import cats.data.EitherT
import cats.syntax.either._
import com.typesafe.scalalogging.{LazyLogging, Logger}
import io.morgaroth.jiraclient
import io.morgaroth.jiraclient._
import io.morgaroth.jiraclient.query.syntax.JiraRequest
import org.slf4j.LoggerFactory
import sttp.client3._
import sttp.client3.httpclient.HttpClientSyncBackend

import scala.concurrent.ExecutionContext
import scala.util.Try

class SttpJiraAPISync(val config: JiraConfig, apiConfig: JiraRestAPIConfig)(implicit ex: ExecutionContext)
    extends JiraRestAPI[cats.Id]
    with LazyLogging {

  override val m: Monad[cats.Id] = cats.catsInstancesForId

  val backend                = HttpClientSyncBackend()
  private val requestsLogger = Logger(LoggerFactory.getLogger(getClass.getPackage.getName + ".requests"))

  override def invokeRequest(requestData: JiraRequest)(implicit requestId: RequestId): EitherT[cats.Id, JiraError, String] = {
    val u = requestData.render
    val requestWithoutPayload = basicRequest
      .method(requestData.method, uri"$u")
      .header("Authorization", requestData.authToken)
      .header("Accept", "application/json")
      .header("User-Agent", "curl/7.61.0")

    val request = requestData.payload
      .map(rawPayload => requestWithoutPayload.body(rawPayload).contentType("application/json"))
      .getOrElse(requestWithoutPayload)

    if (apiConfig.debug) logger.debug(s"request to send: $request")
    requestsLogger.info(s"Request ID {}, request: {}, payload:\n{}", requestId.id, request.body("stripped"), request.body)

    val response = Try(request.send(backend)).toEither
      .leftMap[JiraError](RequestingError("try-http-backend-left", requestId.id, _))
      .flatMap { response =>
        if (apiConfig.debug) logger.debug(s"received response: $response")
        requestsLogger.info(
          s"Request ID {}, response: {}, payload:\n{}",
          requestId,
          response.copy(body = response.body.bimap(_ => "There is an error body", _ => "There is a success body")),
          response.body.fold(identity, identity),
          response.body.fold(identity, identity),
        )
        response.body.leftMap(error =>
          jiraclient.HttpError(response.code.code, "http-response-error", requestId.id, requestId.kind, Some(error)),
        )
      }

    EitherT.fromEither[cats.Id](response)
  }
}
