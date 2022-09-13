package io.gitlab.mateuszjaje.jiraclient
package sttpbackend.zio

import apisv2.ThisMonad
import apisv2.ThisMonad.AAA
import query.syntax.JiraRequest

import com.typesafe.scalalogging.{LazyLogging, Logger}
import org.slf4j.LoggerFactory
import sttp.capabilities
import sttp.capabilities.zio.ZioStreams
import sttp.client3.httpclient.zio.HttpClientZioBackend
import sttp.client3.{SttpBackend, UriContext, basicRequest}
import zio.{Task, UIO, ZIO}

import scala.concurrent.ExecutionContext

class ZSttpJiraAPI(val config: JiraConfig, apiConfig: JiraRestAPIConfig)(implicit ex: ExecutionContext)
  extends JiraRestAPIV2[UIO]
    with LazyLogging {

  implicit override def m: ThisMonad[UIO] = new ThisMonad[UIO] {
    override def subFlatMap[A, B](fa: UIO[Either[JiraError, A]])(f: A => Either[JiraError, B]): AAA[UIO, B] =
      fa.flatMap(x => ZIO.succeed(x.flatMap(f)))

    override def pure[A](x: A): UIO[Either[JiraError, A]] = ZIO.right(x)

    override def flatMap[A, B](fa: AAA[UIO, A])(f: A => AAA[UIO, B]): AAA[UIO, B] = {
      fa.flatMap { (data: Either[JiraError, A]) =>
        data
          .map(f)
          .fold(
            err => ZIO.left(err),
            identity,
          )
      }
    }

    override def tailRecM[A, B](a: A)(f: A => AAA[UIO, Either[A, B]]): AAA[UIO, B] = {
      flatMap(f(a)) {
        case Left(a) => tailRecM(a)(f)
        case Right(b) => pure(b)
      }
    }

    override def sequence[A](x: Vector[UIO[Either[JiraError, A]]]): AAA[UIO, Vector[A]] = {
      ZIO.foreach(x)(identity).map {
        _.foldLeft[Either[JiraError, Vector[A]]](Right(Vector.empty[A])) {
          case (e@Left(_), _) => e
          case (Right(acc), Right(e)) => Right(acc :+ e)
          case (_, Left(e)) => Left(e)
        }
      }
    }
  }

  val backend: Task[SttpBackend[Task, ZioStreams & capabilities.WebSockets]] = HttpClientZioBackend()
  private val requestsLogger = Logger(LoggerFactory.getLogger(getClass.getPackage.getName + ".requests"))

  override def invokeRequest(requestData: JiraRequest)(implicit requestId: RequestId): UIO[Either[JiraError, String]] = {
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

    backend
      .flatMap(request.send(_))
      .mapError[JiraError](RequestingError("zio-http-backend-left", requestId.id, _))
      .flatMap { response =>
        if (apiConfig.debug) logger.debug(s"received response: $response")
        requestsLogger.info(
          s"Request ID {}, response: {}, payload:\n{}",
          requestId,
          response.copy(body = response.body match {
            case Left(_) => "There is an error body"
            case Right(_) => "There is a success body"
          }),
          response.body.fold(identity, identity),
          response.body.fold(identity, identity),
        )
        ZIO
          .fromEither(response.body)
          .mapError(error => HttpError(response.code.code, "http-response-error", requestId.id, requestId.kind, Some(error)))
      }
      .either
  }
}
