package io.morgaroth.jiraclient.marshalling

import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder}
import io.morgaroth.jiraclient.models.Resolution

trait ResolutionCodec {
  implicit val resolutionDecoder: Decoder[Resolution] = Decoder.decodeString.map { raw =>
    // Resolution.byName.get(raw).toRight(s"$raw isn't known jira resolution")
    Resolution.fromRepr(raw)
  }

  implicit val resolutionEncoder: Encoder[Resolution] = Encoder.instance[Resolution](_.name.asJson)
}
