package io.gitlab.mateuszjaje.jiraclient
package sttpbackend

import cats.syntax.either._
import org.scalatest.Assertions.fail

trait HelperClasses {

  implicit class RightValueable[E, V](either: Either[E, V]) {
    def rightValue: V =
      either.valueOr(_ => fail(s"either is $either"))
  }

}
