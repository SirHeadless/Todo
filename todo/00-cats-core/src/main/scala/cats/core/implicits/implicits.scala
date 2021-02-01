package cats.core


package object implicits {
  final implicit class FunctorOps[F[_]: Functor, A](private val fa: F[A]) {
    @inline def map[B](ab: A => B): F[B] = F.map(fa)(ab)

    @inline def as[B](b: => B): F[B] = F.map(fa)(_ => b)
  }

  final implicit class AnyOps[A](private val a: A) {
    @inline def pure[F[_]: Applicative]: F[A] = implicitly[Applicative[F]].pure(a)
  }

  final implicit class EqOpsString[A: Eq](private val a: A) {
    @inline def ===(a2: A): Boolean = implicitly[Eq[A]].eqv(a, a2)
    @inline def =!=(a2: A): Boolean = implicitly[Eq[A]].neqv(a, a2)
  }

  implicit val EqOpsString: Eq[String] = new Eq[String] {
    override def eqv(a1: String, a2: String): Boolean = a1 == a2
  }

  final implicit class TraversOps[F[_]: Traverse, A](private val fa: F[A]) {
    @inline def traverse[G[_]: Applicative, B](abg: A => G[B]): G[F[B]] = F.traverse(fa)(abg)
    @inline def sequence[G[_]: Applicative](gf: F[G[A]]): G[F[A]]  = F.sequence(gf)

  }

  final implicit class FlatMapOps[F[_]: FlatMap, A](private val fa: F[A]) {
    def flatMap[B](afb: A => F[B]): F[B] = F.flatMap(fa)(afb)

    @inline def >>[B](fb: => F[B]): F[B] = F.flatMap(fa)(_ => fb)
  }

  implicit val TraverseForVector: Traverse[Vector] = new Traverse[Vector] {
    override def traverse[G[_] : Applicative, A, B](fa: Vector[A])(agb: A => G[B]): G[Vector[B]] = {

      fa.foldRight(G.pure(Vector.empty): G[Vector[B]]){
        case (a, gbs) => G.map2(gbs, agb(a))((bs,b) => b +: bs)
      }
    }

    override def map[A, B](fa: Vector[A])(ab: A => B): Vector[B] =
      fa.map(ab)
  }

  final implicit class MonadOps[F[_]: Monad, A](private val fa: F[A]) {
    def iterateWhile(p: A => Boolean): F[A] = F.iterateWhile(fa)(p)
  }

}
