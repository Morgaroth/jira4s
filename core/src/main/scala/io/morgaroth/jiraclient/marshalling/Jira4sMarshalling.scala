package io.morgaroth.jiraclient.marshalling

import cats.Monad
import cats.data.EitherT
import cats.syntax.either._
import io.circe._
import io.circe.parser._
import io.circe.syntax.EncoderOps
import io.morgaroth.jiraclient.{JiraError, RequestId, UnmarshallingError}

import scala.language.{higherKinds, implicitConversions}

trait Jira4sMarshalling extends JodaCodec with IssueStatusCodec with ResolutionCodec {

  implicit class Extractable(value: JsonObject) {
    def extract[T](implicit decoder: Decoder[T]): Either[Error, T] = decode[T](value.toString)
  }

  object MJson {
    def read[T](str: String)(implicit d: Decoder[T]): Either[Error, T] = decode[T](str)

    def readT[F[_], T](str: String)(implicit d: Decoder[T], m: Monad[F], requestId: RequestId): EitherT[F, JiraError, T] =
      EitherT.fromEither(read[T](str).leftMap[JiraError](e => UnmarshallingError(e.getMessage, requestId.id, e)))

    def write[T](value: T)(implicit d: Encoder[T]): String = Printer.noSpaces.copy(dropNullValues = true).pretty(value.asJson)

    def writePretty[T](value: T)(implicit d: Encoder[T]): String = printer.pretty(value.asJson)
  }

  // keep all special settings with method write above
  implicit val printer: Printer = Printer.spaces2.copy(dropNullValues = true)
}

object Jira4sMarshalling extends Jira4sMarshalling
