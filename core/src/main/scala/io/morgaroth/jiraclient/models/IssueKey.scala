package io.morgaroth.jiraclient.models

import io.circe.{Decoder, Encoder}

case class IssueKey(value: String) extends AnyVal {
  override def toString = value
}

object IssueKey {
  implicit val encoder: Encoder[IssueKey] = Encoder.encodeString.contramap[IssueKey](_.value)
  implicit val decoder: Decoder[IssueKey] = Decoder.decodeString.map(IssueKey(_))
}