package cats.core

trait Semigroupal[F[_]] {
  def product[A,B](Fa: F[A])(Fb: F[B]): F[(A,B)]
}
