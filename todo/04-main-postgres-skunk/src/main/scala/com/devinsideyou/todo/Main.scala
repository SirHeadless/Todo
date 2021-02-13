package com.devinsideyou
package todo

import cats.effect.IO
import cats.effect.IO._

import java.time.format.DateTimeFormatter
import scala.concurrent.ExecutionContext

import natchez.Trace.Implicits.noop

object Main extends App {
  type F[A] = IO[A]

  import cats.effect._

  implicit val cs: ContextShift[IO] =
    IO.contextShift(ExecutionContext.global)



  Program.dsl[F].unsafeRunSync()
}
