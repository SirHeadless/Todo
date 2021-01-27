package cats.effect

import cats.core._

trait Sync[F[_]] extends Monad[F] with Defer[F] {
  def delay[A](a: => A): F[A] = defer(pure(a))
}
