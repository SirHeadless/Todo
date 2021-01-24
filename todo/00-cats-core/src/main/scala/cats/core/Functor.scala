package cats.core

trait Functor[F[_]] {
  def map[A,B](f: F[_])(ab: A => B): F[B]
}
