package com.devinsideyou
package todo

import cats.effect.IO
import cats.effect.IO._

import java.time.format.DateTimeFormatter


object Main extends App {
  type F[A] = IO[A]

  Program.dsl[F].unsafeRunSync()
}
