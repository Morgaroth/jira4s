package io.gitlab.mateuszjaje.jiraclient
package apisv2

import apisv2.JiraApiT.JiraApiTType

import cats.Monad

object JiraApiT {
  type JiraApiTType[F[_], A] = F[Either[JiraError, A]]

  implicit def fromCats[F[_]](implicit m: Monad[F]): JiraApiT[F] = new JiraApiT[F] {
    import cats.syntax.applicative.*
    import cats.syntax.either.*

    override def pure[A](x: A): F[Either[JiraError, A]] = m.pure(Right(x))

    override def flatMap[A, B](fa: F[Either[JiraError, A]])(f: A => F[Either[JiraError, B]]): F[Either[JiraError, B]] = {
      m.flatMap(fa) { (data: Either[JiraError, A]) =>
        val s1: Either[JiraError, F[Either[JiraError, B]]] = data.map(f)
        s1.fold(
          _.asLeft.pure[F],
          identity,
        )
      }
    }

    override def subFlatMap[A, B](fa: F[Either[JiraError, A]])(f: A => Either[JiraError, B]): F[Either[JiraError, B]] =
      m.map(fa)((data: Either[JiraError, A]) => data.flatMap(f))

    override def tailRecM[A, B](a: A)(f: A => F[Either[JiraError, Either[A, B]]]): F[Either[JiraError, B]] = {
      flatMap(f(a)) {
        case Left(a)  => tailRecM(a)(f)
        case Right(b) => pure(b)
      }
    }

    override def sequence[A](x: Vector[F[Either[JiraError, A]]]): F[Either[JiraError, Vector[A]]] = {
      import cats.syntax.traverse.*
      m.map(x.sequence)(_.foldLeft(Vector.empty[A].asRight[JiraError]) {
        case (e @ Left(_), _)       => e
        case (Right(acc), Right(e)) => Right(acc :+ e)
        case (_, Left(e))           => e.asLeft
      })
    }

  }

  trait Ops[F[_], A] extends Serializable {
    def self: F[Either[JiraError, A]]

    val typeClassInstance: JiraApiT[F]

    def map[B](f: A => B): F[Either[JiraError, B]] = typeClassInstance.map[A, B](self)(f)

    def flatMap[B](f: A => F[Either[JiraError, B]]): F[Either[JiraError, B]]               = typeClassInstance.flatMap[A, B](self)(f)
    def subFlatMap[B](f: A => Either[JiraError, B]): F[Either[JiraError, B]] = typeClassInstance.subFlatMap(self)(f)
  }

  trait PureOps[A] extends Serializable {
    def self: A

    def pure[F[_]](implicit thisMonad2: JiraApiT[F]): F[Either[JiraError, A]] = thisMonad2.pure(self)
  }

  object syntax {

    implicit def toOps2[F[_], A](target: F[Either[JiraError, A]])(implicit m: JiraApiT[F]): Ops[F, A] = new Ops[F, A] {
      override val self: F[Either[JiraError, A]] = target

      override val typeClassInstance: JiraApiT[F] = m
    }

    def toOps[F[_], A](target: F[Either[JiraError, A]])(implicit m: JiraApiT[F]): Ops[F, A] = new Ops[F, A] {
      override val self: F[Either[JiraError, A]] = target

      override val typeClassInstance: JiraApiT[F] = m
    }

    implicit def pureOps[A](target: A): PureOps[A] = new PureOps[A] {
      override def self: A = target
    }

  }

}

trait JiraApiT[F[_]] extends Monad[JiraApiTType[F, *]] {
  def subFlatMap[A, B](fa: F[Either[JiraError, A]])(f: A => Either[JiraError, B]): F[Either[JiraError, B]]

  override def pure[A](x: A): F[Either[JiraError, A]]

  override def flatMap[A, B](fa: F[Either[JiraError, A]])(f: A => F[Either[JiraError, B]]): F[Either[JiraError, B]]

  override def tailRecM[A, B](a: A)(f: A => F[Either[JiraError, Either[A, B]]]): F[Either[JiraError, B]]

  def sequence[A](x: Vector[F[Either[JiraError, A]]]): F[Either[JiraError, Vector[A]]]
}
