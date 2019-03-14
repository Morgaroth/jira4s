package io.morgaroth.jiraclient.marshalling

import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder}
import io.morgaroth.jiraclient.models.IssueStatus

trait IssueStatusCodec {
  implicit val mergeStrategyDecoder: Decoder[IssueStatus] = Decoder.decodeString.emap { raw =>
    IssueStatus.byName.get(raw).toRight(s"$raw isn't known jira status")
  }

  implicit val mergeStrategyEncoder: Encoder[IssueStatus] = Encoder.instance[IssueStatus](_.repr.asJson)
}

object IssueStatusCodec extends IssueStatusCodec
