package io.morgaroth.jiraclient

import io.circe.generic.extras.Configuration

package object marshalling {
  implicit val config: Configuration = Configuration.default
}
