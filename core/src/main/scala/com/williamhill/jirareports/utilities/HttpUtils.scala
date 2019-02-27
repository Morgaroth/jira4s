package com.williamhill.jirareports.utilities

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.Materializer
import akka.util.Timeout
import cats.data.EitherT
import cats.implicits._
import com.williamhill.jirareports.akkahttp.AkkaHttpExtensions
import io.circe.Decoder
import io.morgaroth.jiraclient.HttpCirceSupport

import scala.concurrent.Future
import scala.concurrent.duration._

trait HttpCallFailure

case class ServiceFailure(reason: String) extends HttpCallFailure

case class UnmarshallingFailure(reason: String) extends HttpCallFailure

trait HttpUtils extends HttpCirceSupport with AkkaHttpExtensions {

//  private val allRequests = Kamon.metrics.counter("jira-requests")

  def doRequest[T](request: HttpRequest, name: String)
                  (implicit as: ActorSystem, mat: Materializer, tm: Timeout, decoder: Decoder[T])
  : Future[Either[HttpCallFailure, T]] = {

//    allRequests.increment()
//    Kamon.metrics.counter(s"$name-requests").increment()
    (for {
      response <- EitherT(singleRequest(request)).leftMap(uhr => ServiceFailure("http failure: " + uhr.toString))
      unm      <- EitherT(unmarshall[T](response.resp, request.toString()))
    } yield unm).value
  }

  private def unmarshall[T](response: HttpResponse, s: String)
                           (implicit as: ActorSystem, mat: Materializer, decoder: Decoder[T])
  : Future[Either[HttpCallFailure, T]] = {
    response.entity.toStrict(1.minute)
      .map(_.data.decodeString("utf-8")).map(MJson.read[T])
      .map(_.leftMap(x => UnmarshallingFailure(x.toString)))
      .recover {
        case th => UnmarshallingFailure(th.toString).asLeft
      }
  }
}
