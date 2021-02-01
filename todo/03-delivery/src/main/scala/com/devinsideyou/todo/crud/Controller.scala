package com.devinsideyou
package todo
package crud

import cats.core._
import cats.core.implicits._

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

trait Controller[F[_]] {
  def programm: F[Unit]
}

object Controller {
  def dsl[F[_] : Monad](
                         persistenceService: TodoPersistenceService[F],
                         pattern: DateTimeFormatter,
                         console: FancyConsole[F],
                         random: Random[F]
                       ): Controller[F] =
    new Controller[F] {
      override def programm: F[Unit] = {
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
          random.nextInt(colors.size).map(colors)

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
          menu.flatMap(console.getStrLnTrimmedWithPrompt)

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
        console.getStrLnTrimmedWithPrompt("Please enter a description:")


      private val create: F[Unit] =
        descriptionPrompt.flatMap { description =>
          withDeadlinePrompt { deadline =>
            persistenceService
              .createOne(Todo.Data(description, deadline))
              .>>(console.putSuccess("Successfully created the new todo."))
          }
        }

      private val deadlinePrompt: F[String] =
        console.getStrLnTrimmedWithPrompt(
          s"Please enter a deadline in the following format $DeadlinePromptFormat:"
        )

      private def withDeadlinePrompt(onSuccess: LocalDateTime => F[Unit]): F[Unit] =
        deadlinePrompt.map(toLocalDateTime).flatMap {
          case Right(deadline) => onSuccess(deadline)
          case Left(error) => console.putError(error)
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

      private val idPrompt: F[String] =
        console.getStrLnTrimmedWithPrompt("Please enter the id:")


      private val delete: F[Unit] =
        withIdPrompt { id =>
          withReadOne(id) { todo =>
            persistenceService
              .deleteOne(todo)
              .tapAs(console.putSuccess("Successfully deleted the todo."))
          }
        }

      private def withIdPrompt(onValidId: String => Unit): F[Unit] =
        idPrompt.map(toId).flatMap {
          case Right(id) => F.pure(onValidId(id))
          case Left(error) => console.putError(error)
        }

      private def toId(userInput: String): Either[String, String] =
        if (userInput.isEmpty || userInput.contains(" "))
          Left(
            s"\n${scala.Console.YELLOW + userInput + scala.Console.RED} is not a valid id.${scala.Console.RESET}"
          )
        else
          Right(userInput)

      private val deleteAll: F[Unit] =
        persistenceService
          .deleteAll
          .>>(console.putSuccess("Successfully deleted all todos."))

      private def withReadOne(id: String)(onFound: Todo.Existing => Unit): F[Unit] =
        persistenceService
          .readOneById(id)
          .map {
            case Some(todo) => onFound(todo)
            case None => displayNoTodosFoundMessage
          }

      private val showAll: F[Unit] =
        persistenceService.readAll.flatMap(displayZeroOrMany)

      private def displayZeroOrMany(todos: Vector[Todo.Existing]): F[Unit] =
        if (todos.isEmpty)
          displayNoTodosFoundMessage
        else {
          val uxMatters = if (todos.size == 1) "todo" else "todos"

          val renderedSize: String =
            inColor(todos.size.toString)(scala.Console.GREEN)

          console.putStrLn(s"\nFound $renderedSize $uxMatters:\n") >>
            todos
              .sortBy(_.deadline)
              .map(renderedWithPattern)
              .traverse(console.putStrLn)
              .map(_ => ())

        }

      private val displayNoTodosFoundMessage: F[Unit] =
        console.putWarning("\nNo todos found!")

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
          .flatMap(persistenceService.readManyByPartialDescription)
          .flatMap(displayZeroOrMany)

      private val searchById: F[Unit] =
        withIdPrompt { id =>
          persistenceService
            .readOneById(id)
            .map(_.to(Vector))
            .flatMap(displayZeroOrMany)
        }

      private val updateDescription: F[Unit] =
        withIdPrompt { id =>
          withReadOne(id) { todo =>
            descriptionPrompt.flatMap { description =>
              persistenceService
                .updateOne(todo.withUpdatedDescription(description)) >>
                console.putSuccess("Successfully updated the description.")
            }
          }
        }

      private val updateDeadline: F[Unit] =
        withIdPrompt { id =>
          withReadOne(id) { todo =>
            withDeadlinePrompt { deadline =>
              persistenceService
                .updateOne(todo.withUpdatedDeadline(deadline))
                .>>(console.putSuccess("Successfully updated the deadline."))
            }
          }
        }

      private val exit: F[Unit] =
        console.putStrLn("\nUntil next time!\n")


    }

  private val DeadlinePromptPattern: String =
    "yyyy-M-d H:m"

  private val DeadlinePromptFormat: String =
    inColor(DeadlinePromptPattern)(scala.Console.MAGENTA)

  private def inColor(line: String)(color: String): String =
    color + line + scala.Console.RESET

}
