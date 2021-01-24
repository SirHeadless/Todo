package cats.core


package object implicits {
  final implicit class FunctorOps[F[_]: Functor, A](
                                                     private val fa: F[A]
                                                   ) {
    @inline def map[B](ab: A => B): F[B] = F.map(fa)(ab)
  }

  final implicit class AnyOps[A](private val a: A) {
    @inline def pure[F[_]: Applicative]: F[A] = implicitly[Applicative[F]].pure(a)
  }
}
