package com.williamhill.jirareports.utilities

import com.typesafe.scalalogging.{LazyLogging, Logger}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions
import scala.util.{Failure, Success, Try}

case class LoggableFuture[T](f: Future[T], logger: Logger)(implicit ex: ExecutionContext) {
  def logErrors(msg: String): Future[T] = {
    f.recover {
      case t: Throwable =>
        logger.error(msg, t)
        throw t
    }
  }

  def logWarnings(msg: String): Future[T] = {
    f.recover {
      case t: Throwable =>
        logger.warn(msg, t)
        throw t
    }
  }

  def logging(func: PartialFunction[Try[T], Unit]): Future[T] = {
    f.onComplete(func)
    f
  }
}

trait FutureHelpers extends LazyLogging {
  implicit def convertFutureToBeLoggableException[T](f: Future[T])(implicit ac: ExecutionContext): LoggableFuture[T] = {
    LoggableFuture(f, logger)
  }

  implicit def wrapToFutureIfNeeded[T](value: T): Future[T] = success(value)

  type DU = DummyImplicit

  def success[T](value: T)(implicit _1: DU): Future[T] = Future.successful(value)

  def success[T](value: T)(implicit _1: DU, _2: DU): Option[T] = Some(value)

  def success[T](value: T)(implicit _1: DU, _2: DU, _3: DU): Try[T] = Success(value)

  def failure(value: Throwable)(implicit _1: DU): Future[Nothing] = Future.failed(value)

  def failure(value: Throwable)(implicit _1: DU, _2: DU): Try[Nothing] = Failure(value)

  def failed(value: Throwable)(implicit _1: DU): Future[Nothing] = Future.failed(value)

  def failed(value: Throwable)(implicit _1: DU, _2: DU): Try[Nothing] = Failure(value)

  def FSuccess[T](value: T): Future[T] = Future.successful(value)

  def OSuccess[T](value: T): Option[T] = Some(value)

  def TSuccess[T](value: T): Try[T] = Success(value)

  def FFailure(value: Throwable): Future[Nothing] = Future.failed(value)

  def TFailure(value: Throwable): Try[Nothing] = Failure(value)
}
