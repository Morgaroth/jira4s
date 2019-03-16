package io.morgaroth.jiraclient.marshalling

import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder}
import io.morgaroth.jiraclient.models.IssueStatus

trait IssueStatusCodec {
  implicit val mergeStrategyDecoder: Decoder[IssueStatus] = Decoder.decodeString.map { raw =>
    // IssueStatus.byName.get(raw).toRight(s"$raw isn't known jira status")
    IssueStatus.fromRepr(raw)
  }

  implicit val mergeStrategyEncoder: Encoder[IssueStatus] = Encoder.instance[IssueStatus](_.name.asJson)
}

object IssueStatusCodec extends IssueStatusCodec
