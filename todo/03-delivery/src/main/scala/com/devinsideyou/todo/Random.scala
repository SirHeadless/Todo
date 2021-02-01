package com.devinsideyou
package todo

import cats.effect._

trait Random[F[_]] {
  def nextInt(n: Int): F[Int]
}

object Random {
  def dsl[F[_]: Sync]: Random[F] =
    new Random[F] {
      override def nextInt(n: Int): F[Int] =
        F.delay(scala.util.Random.nextInt(n))
    }
}
