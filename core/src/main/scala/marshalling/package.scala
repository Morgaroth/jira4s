package io.gitlab.mateuszjaje
package jiraclient

import io.circe.generic.extras.Configuration

package object marshalling {
  implicit val config: Configuration = Configuration.default
}
