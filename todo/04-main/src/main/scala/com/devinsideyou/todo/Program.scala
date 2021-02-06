package com.devinsideyou.todo

import cats._
import cats.implicits._
import cats._

import java.time.format.DateTimeFormatter

object Program {
  def dsl[F[_]: effect.Sync]: F[Unit] = {
    for {
      console <- Console.dsl
      random <- Random.dsl
      controller <- crud.DependencyGraph.dsl(Pattern, console, random)
      _ <- controller.programm
    } yield ()
  }

  private val Pattern =
    DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy HH:mm")
}
