package cats.core

trait Eq[A] {

  def eqv(a1: A, a2: A): Boolean

  def neqv(a1: A, a2: A): Boolean =
    !eqv(a1, a2)
}