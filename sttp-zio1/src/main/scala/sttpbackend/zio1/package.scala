package io.gitlab.mateuszjaje.jiraclient
package sttpbackend

import io.gitlab.mateuszjaje.jiraclient.query.syntax.{Method, Methods}

package object zio1 {

  implicit def convertJiraMethodToSttpMethod(in: Method): sttp.model.Method = in match {
    case Methods.Get    => sttp.model.Method.GET
    case Methods.Post   => sttp.model.Method.POST
    case Methods.Put    => sttp.model.Method.PUT
    case Methods.Delete => sttp.model.Method.DELETE
  }
}
