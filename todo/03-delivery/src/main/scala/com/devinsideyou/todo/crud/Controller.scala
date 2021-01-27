package com.devinsideyou
package todo
package crud

import cats.core._
import cats.core.implicits._

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

trait Controller[F[_]] {
  def run(): Unit
}

object Controller {
  def dsl[F[_] : FancyConsole : Random : Monad](
                                                 boundary: Boundary[F],
                                                 pattern: DateTimeFormatter
                                               ): Controller[F] =
    new Controller[F] {
      override def run(): Unit = {
        val colors: Vector[String] =
          Vector(
            // scala.Console.BLACK,
            scala.Console.BLUE,
            scala.Console.CYAN,
            scala.Console.GREEN,
            scala.Console.MAGENTA,
            scala.Console.RED,
            // scala.Console.WHITE,
            scala.Console.YELLOW
          )

        def randomColor: F[String] =
          F.nextInt(colors.size).map(colors)

        def hyphens: F[String] = {
          randomColor.map(inColor("─" * 100)(_))
        }

        def menu: F[String] = {
          hyphens.map { hyphens =>
            s"""|
                |$hyphens
                |
                |c                   => create new todo
                |d                   => delete todo
                |da                  => delete all todos
                |sa                  => show all todos
                |sd                  => search by partial description
                |sid                 => search by id
                |ud                  => update description
                |udl                 => update deadline
                |e | q | exit | quit => exit the application
                |anything else       => show the main menu
                |
                |Please enter a command:""".stripMargin
          }
        }

        val prompt: F[String] =
          menu.flatMap(F.getStrLnTrimmedWithPrompt(_))

        prompt
          .flatMap {
          case "c" => create.as(true)
          case "d" => delete.as(true)
          case "da" => deleteAll.as(true)
          case "sa" => showAll.as(true)
          case "sd" => searchByPartialDescription.as(true)
          case "sid" => searchById.as(true)
          case "ud" => updateDescription.as(true)
          case "udl" => updateDeadline.as(true)
          case "e" | "q" | "exit" | "quit" => exit.as(false)
          case _ => F.pure(true)
          }
          .iterateWhile(identity)
          .map(_ => ())


      }

      private val descriptionPrompt: F[String] =
        F.getStrLnTrimmedWithPrompt("Please enter a description:")

      private val create: F[Unit] =
        descriptionPrompt.map { description =>
          withDeadlinePrompt { deadline =>
            boundary
              .createOne(Todo.Data(description, deadline))
              .>>(F.putSuccess("Successfully created the new todo."))
          }
        }

      private val deadlinePrompt: F[String] =
        F.getStrLnTrimmedWithPrompt(
          s"Please enter a deadline in the following format $DeadlinePromptFormat:"
        )

      private def withDeadlinePrompt(onSuccess: LocalDateTime => Unit): F[Unit] =
        deadlinePrompt.map(toLocalDateTime).flatMap {
          case Right(deadline) => F.pure(onSuccess(deadline))
          case Left(error) => F.putError(error)
        }

      private def toLocalDateTime(input: String): Either[String, LocalDateTime] = {
        val formatter = DateTimeFormatter.ofPattern(DeadlinePromptPattern)

        scala
          .util
          .Try(LocalDateTime.parse(input, formatter))
          .toEither
          .left
          .map { _ =>
            s"\n${inColor(input)(scala.Console.YELLOW)} does not match the required format $DeadlinePromptFormat.${scala.Console.RESET}"
          }
      }

      private val delete: F[Unit] =
        withIdPrompt { id =>
          withReadOne(id) { todo =>
            boundary
              .deleteOne(todo)
              .tapAs(F.putSuccess("Successfully deleted the todo."))
          }
        }

      private val idPrompt: F[String] =
        F.getStrLnTrimmedWithPrompt("Please enter the id:")

      private def withIdPrompt(onValidId: String => Unit): F[Unit] =
        idPrompt.map(toId).flatMap {
          case Right(id) => F.pure(onValidId(id))
          case Left(error) => F.putError(error)
        }

      private def toId(userInput: String): Either[String, String] =
        if (userInput.isEmpty || userInput.contains(" "))
          Left(
            s"\n${scala.Console.YELLOW + userInput + scala.Console.RED} is not a valid id.${scala.Console.RESET}"
          )
        else
          Right(userInput)

      private val deleteAll: F[Unit] =
        boundary
          .deleteAll
          .>>(F.putSuccess("Successfully deleted all todos."))

      private def withReadOne(id: String)(onFound: Todo.Existing => Unit): F[Unit] =
        boundary
          .readOneById(id)
          .map {
            case Some(todo) => onFound(todo)
            case None => displayNoTodosFoundMessage
          }

      private val showAll: F[Unit] =
        boundary.readAll.flatMap(displayZeroOrMany)

      private def displayZeroOrMany(todos: Vector[Todo.Existing]): F[Unit] =
        if (todos.isEmpty)
          displayNoTodosFoundMessage
        else {
          val uxMatters = if (todos.size == 1) "todo" else "todos"

          val renderedSize: String =
            inColor(todos.size.toString)(scala.Console.GREEN)

          F.putStrLn(s"\nFound $renderedSize $uxMatters:\n") >>
            todos
              .sortBy(_.deadline)
              .map(renderedWithPattern)
              .traverse(F.putStrLn)
              .map(_ => ())

        }

      private val displayNoTodosFoundMessage: F[Unit] =
        F.putWarning("\nNo todos found!")

      private def renderedWithPattern(todo: Todo.Existing): String = {
        def renderedId: String =
          inColor(todo.id)(scala.Console.GREEN)

        def renderedDescription: String =
          inColor(todo.description)(scala.Console.MAGENTA)

        def renderedDeadline: String =
          inColor(todo.deadline.format(pattern))(scala.Console.YELLOW)

        s"$renderedId $renderedDescription is due on $renderedDeadline."
      }

      private val searchByPartialDescription: F[Unit] =
        descriptionPrompt
          .flatMap(boundary.readManyByPartialDescription)
          .flatMap(displayZeroOrMany)

      private val searchById: F[Unit] =
        withIdPrompt { id =>
          boundary
            .readOneById(id)
            .map(_.to(Vector))
            .flatMap(displayZeroOrMany)
        }

      private val updateDescription: F[Unit] =
        withIdPrompt { id =>
          withReadOne(id) { todo =>
            descriptionPrompt.flatMap { description =>
              boundary
                .updateOne(todo.withUpdatedDescription(description)) >>
                F.putSuccess("Successfully updated the description.")
            }
          }
        }

      private val updateDeadline: F[Unit] =
        withIdPrompt { id =>
          withReadOne(id) { todo =>
            withDeadlinePrompt { deadline =>
              boundary
                .updateOne(todo.withUpdatedDeadline(deadline))
                .tapAs(F.putSuccess("Successfully updated the deadline."))
            }
          }
        }

      private val exit: F[Unit] =
        F.putStrLn("\nUntil next time!\n")

      private val DeadlinePromptPattern: String =
        "yyyy-M-d H:m"

      private val DeadlinePromptFormat: String =
        inColor(DeadlinePromptPattern)(scala.Console.MAGENTA)

      private def inColor(line: String)(color: String): String =
        color + line + scala.Console.RESET
    }
}
