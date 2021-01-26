package cats.core

trait Apply[F[_]] extends Semigroupal[F] with Functor[F]{

  def map2[A,B, Result](fa: F[A], fb: F[B])(abr: (A,B) => Result): F[Result] =
    map[(A,B),Result](product(fa)(fb)) { case (a, b) => abr(a, b) }
  //    map(product(fa)(fb))(abr.tupled)
}
