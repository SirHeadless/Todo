package com.devinsideyou.todo

import cats._
import cats.implicits._
import cats._
import cats.effect._

import java.time.format.DateTimeFormatter

object Program {
  def dsl[F[_]: Concurrent: ContextShift: natchez.Trace]: F[Unit] = {
    SessionPool.dsl.use { resource =>
      for {
        console <- Console.dsl
        random <- Random.dsl
        controller <- crud.DependencyGraph.dsl(Pattern, console, random, resource)
        _ <- controller.programm
      } yield ()
    }
  }

  private val Pattern =
    DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy HH:mm")
}
