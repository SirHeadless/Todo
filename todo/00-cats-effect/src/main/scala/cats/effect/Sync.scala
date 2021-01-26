package cats.effect

import cats.core.{Applicative, Defer}

trait Sync[F[_]] extends Applicative[F] with Defer[F] {
  def delay[A](a: => A): F[A] = defer(pure(a))
}
