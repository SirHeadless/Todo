package cats.core

trait Traverse[F[_]] extends Functor[F]{
  def traverse[G[_]: Applicative, A, B](fa: F[A])(abg: A => G[B]): G[F[B]]
  def sequence[G[_]: Applicative, A](gf: F[G[A]]): G[F[A]]  =
    traverse(gf)(identity)
}
