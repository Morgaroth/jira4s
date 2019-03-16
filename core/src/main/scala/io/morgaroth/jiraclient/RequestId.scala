package io.morgaroth.jiraclient

import java.util.UUID

case class RequestId(id: String)

object RequestId {
  def newOne = new RequestId(UUID.randomUUID().toString)
}