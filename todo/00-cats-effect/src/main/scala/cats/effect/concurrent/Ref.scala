package cats.effect.concurrent

import cats.effect.Sync

import java.util.concurrent.atomic.AtomicReference
import scala.annotation.tailrec

import cats.core.implicits._

trait Ref[F[_], A] {
  def set(a: A): F[Unit]

  def get: F[A]

  def update(aa: A => A): F[Unit]

  def updateAndGet(aa: A => A): F[A]

  def modify[B](aab: A => (A, B)): F[B]
}

object Ref {

  def of[F[_] : Sync, A](a: A): F[Ref[F, A]] =
    F.delay {
      new Ref[F, A] {
        private[this] val state: AtomicReference[A] = new AtomicReference(a)

        override def modify[B](aab: A => (A, B)): F[B] = {
          @tailrec
          def loop: B = {
            val currentVal: A = state.get
            val (nextVal, result) = aab(currentVal)
            if (state.compareAndSet(currentVal, nextVal)) {
              result
            } else {
              loop
            }
          }

          F.delay(loop)
        }

        override def set(a: A): F[Unit] = F.delay(state.set(a))

        override def get: F[A] = F.delay(state.get)

        override def update(aa: A => A): F[Unit] = updateAndGet(aa).map(_ => ())

        override def updateAndGet(aa: A => A): F[A] = modify { a =>
          val desired = aa(a)
          (desired, desired)
        }
      }
    }
}
