package com.devinsideyou.todo

import cats.effect.{IO, Sync}

import java.time.format.DateTimeFormatter

object Program {
  def dsl[F[_]: Sync]: F[Unit] = {
    val crudController: crud.Controller[F] =
      crud.DependencyGraph.dsl(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy HH:mm"), Console.dsl,Random.dsl)

    val program: F[Unit] = crudController.programm

    println(s"[${scala.Console.YELLOW}warn${scala.Console.RESET}] Any output before this line is a bug")

    program
  }
}
