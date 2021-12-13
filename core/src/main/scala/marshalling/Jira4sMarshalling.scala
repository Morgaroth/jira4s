package io.gitlab.mateuszjaje.jiraclient
package marshalling

import cats.Monad
import cats.data.EitherT
import cats.syntax.either._
import io.circe._
import io.circe.parser._
import io.circe.syntax.EncoderOps

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import scala.language.{higherKinds, implicitConversions}

trait Jira4sMarshalling {

  implicit class Extractable(value: JsonObject) {
    def extract[T](implicit decoder: Decoder[T]): Either[Error, T] = decode[T](value.toString)
  }

  private val zonedDateTimeDecoders = Vector(
    Decoder.decodeZonedDateTime,
    Decoder.decodeZonedDateTimeWithFormatter(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ")),
  ).reduce(_ or _)
  implicit val zonedDateTimeCodec: Codec[ZonedDateTime] = Codec.from(zonedDateTimeDecoders, Encoder.encodeZonedDateTime)

  object MJson {
    def read[T](str: String)(implicit d: Decoder[T]): Either[Error, T] = decode[T](str)

    def readT[F[_], T](str: String)(implicit d: Decoder[T], m: Monad[F], requestId: RequestId): EitherT[F, JiraError, T] =
      EitherT.fromEither(read[T](str).leftMap[JiraError](e => UnmarshallingError(e.getMessage, requestId.id, e)))

    def write[T](value: T)(implicit d: Encoder[T]): String = Printer.noSpaces.copy(dropNullValues = true).print(value.asJson)

    def writePretty[T](value: T)(implicit d: Encoder[T]): String = printer.print(value.asJson)
  }

  // keep all special settings with method write above
  implicit val printer: Printer = Printer.spaces2.copy(dropNullValues = true)

  implicit class unmarshallEitherT[F[_]](data: EitherT[F, JiraError, String])(implicit m: Monad[F]) {
    def unmarshall[TargetType: Decoder](implicit rId: RequestId): EitherT[F, JiraError, TargetType] =
      data.flatMap(MJson.readT[F, TargetType])
  }

}

object Jira4sMarshalling extends Jira4sMarshalling
