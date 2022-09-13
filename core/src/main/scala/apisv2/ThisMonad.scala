package io.gitlab.mateuszjaje.jiraclient
package apisv2

import apisv2.ThisMonad.AAA

import cats.Monad

object ThisMonad {
  type AAA[F[_], A] = F[Either[JiraError, A]]

  implicit def fromCats[F[_]](implicit m: Monad[F]): ThisMonad[F] = new ThisMonad[F] {
    import cats.syntax.applicative.*
    import cats.syntax.either.*

    override def pure[A](x: A): F[Either[JiraError, A]] = m.pure(Right(x))

    override def flatMap[A, B](fa: AAA[F, A])(f: A => AAA[F, B]): AAA[F, B] = {
      m.flatMap(fa) { (data: Either[JiraError, A]) =>
        val s1: Either[JiraError, F[Either[JiraError, B]]] = data.map(f)
        s1.fold(
          _.asLeft.pure[F],
          identity,
        )
      }
    }

    override def subFlatMap[A, B](fa: F[Either[JiraError, A]])(f: A => Either[JiraError, B]): AAA[F, B] =
      m.map(fa)((data: Either[JiraError, A]) => data.flatMap(f))

    override def tailRecM[A, B](a: A)(f: A => AAA[F, Either[A, B]]): AAA[F, B] = {
      flatMap(f(a)) {
        case Left(a)  => tailRecM(a)(f)
        case Right(b) => pure(b)
      }
    }

    override def sequence[A](x: Vector[F[Either[JiraError, A]]]): AAA[F, Vector[A]] = {
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

    val typeClassInstance: ThisMonad[F]

    def map[B](f: A => B): AAA[F, B] = typeClassInstance.map[A, B](self)(f)

    def flatMap[B](f: A => AAA[F, B]): AAA[F, B]                 = typeClassInstance.flatMap[A, B](self)(f)
    def subFlatMap[B](f: A => Either[JiraError, B]): AAA[F, B] = typeClassInstance.subFlatMap(self)(f)
  }

  trait PureOps[A] extends Serializable {
    def self: A

    def pure[F[_]](implicit thisMonad2: ThisMonad[F]): F[Either[JiraError, A]] = thisMonad2.pure(self)
  }

  object syntax {

    implicit def toOps2[F[_], A](target: F[Either[JiraError, A]])(implicit m: ThisMonad[F]): Ops[F, A] = new Ops[F, A] {
      override val self: F[Either[JiraError, A]] = target

      override val typeClassInstance: ThisMonad[F] = m
    }

    def toOps[F[_], A](target: F[Either[JiraError, A]])(implicit m: ThisMonad[F]): Ops[F, A] = new Ops[F, A] {
      override val self: F[Either[JiraError, A]] = target

      override val typeClassInstance: ThisMonad[F] = m
    }

    implicit def pureOps[A](target: A): PureOps[A] = new PureOps[A] {
      override def self: A = target
    }

  }

}

trait ThisMonad[F[_]] extends Monad[AAA[F, *]] {
  def subFlatMap[A, B](fa: F[Either[JiraError, A]])(f: A => Either[JiraError, B]): AAA[F, B]

  override def pure[A](x: A): F[Either[JiraError, A]]

  override def flatMap[A, B](fa: AAA[F, A])(f: A => AAA[F, B]): AAA[F, B]

  override def tailRecM[A, B](a: A)(f: A => AAA[F, Either[A, B]]): AAA[F, B]

  def sequence[A](x: Vector[F[Either[JiraError, A]]]): AAA[F, Vector[A]]
}
