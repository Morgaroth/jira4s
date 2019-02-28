package io.morgaroth.jiraclient

import com.softwaremill.sttp
import io.morgaroth.jiraclient.query.syntax.{Method, Methods}

import scala.language.implicitConversions

package object sttpbackend {

  implicit def convertJiraMethodToSttpMethod(in: Method): sttp.Method = {
    if (in == Methods.Get) sttp.Method.GET else {
      if (in == Methods.Post) sttp.Method.POST else {
        if (in == Methods.Put) sttp.Method.PUT else {
          if (in == Methods.Delete) sttp.Method.DELETE else {
            ???
          }
        }
      }
    }
  }
}
