package cats.core

trait Functor[F[_]] {
  def map[A,B](f: F[A])(ab: A => B): F[B]
}
