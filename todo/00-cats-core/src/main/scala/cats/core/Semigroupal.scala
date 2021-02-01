package cats.core

trait Semigroupal[F[_]] {
  def product[A,B](fa: F[A])(fb: F[B]): F[(A,B)]
}
