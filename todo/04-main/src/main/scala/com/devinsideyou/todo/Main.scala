package com.devinsideyou
package todo

import cats.effect.IO
import cats.effect.IO._

import java.time.format.DateTimeFormatter


object Main extends App {
  type F[A] = IO[A]

  val crudController: crud.Controller[F] =
    crud.DependencyGraph.dsl(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy HH:mm"), Console.dsl,Random.dsl)

  val program: F[Unit] = crudController.programm


  println(s"[${scala.Console.YELLOW}warn${scala.Console.RESET}] Any output before this line is a bug")
  program.unsafeRunSync()
}
