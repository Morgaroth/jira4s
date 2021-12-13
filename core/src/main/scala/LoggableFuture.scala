package io.gitlab.mateuszjaje.jiraclient

import com.typesafe.scalalogging.Logger

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import scala.util.Try

case class LoggableFuture[T](f: Future[T], logger: Logger)(implicit ex: ExecutionContext) {
  def logErrors(msg: String): Future[T] = {
    f.recover { case t: Throwable =>
      logger.error(msg, t)
      throw t
    }
  }

  def logWarnings(msg: String): Future[T] = {
    f.recover { case t: Throwable =>
      logger.warn(msg, t)
      throw t
    }
  }

  def logging(func: PartialFunction[Try[T], Unit]): Future[T] = {
    f.onComplete(func)
    f
  }
}
