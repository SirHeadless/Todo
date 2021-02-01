package com.devinsideyou
package todo
package crud

import cats.core.implicits._
import cats.effect.Sync

object InMemoryTodoRepository {
  def dsl[F[_]: Sync] : TodoRepository[F] = {
    new TodoRepository[F] {
      var nextId: Int = 0
      var state: Vector[Todo.Existing] = Vector.empty

      override def writeMany(todos: Vector[Todo]): F[Vector[Todo.Existing]] = {
        val test = todos.traverse(writeOne)
        test
      }

      private def writeOne(todo: Todo): F[Todo.Existing] =
        todo match {
          case item: Todo.Data     => createOne(item)
          case item: Todo.Existing => updateOne(item)
        }

      private def createOne(todo: Todo.Data): F[Todo.Existing] = F.delay{
        val created =
          Todo.Existing(
            id = nextId.toString,
            data = todo
          )

        state :+= created

        nextId += 1

        created
      }

      override def readManyById(ids: Vector[String]): F[Vector[Todo.Existing]] =
        F.delay(state.filter(todo => ids.contains(todo.id)))

      override def readManyByPartialDescription(partialDescription: String): F[Vector[Todo.Existing]] = {
        F.delay {
          state.filter(
            _.description
              .toLowerCase
              .contains(partialDescription.toLowerCase)
          )
        }
      }

      override def readAll: F[Vector[Todo.Existing]] =
        F.delay(state)

      private def updateOne(todo: Todo.Existing): F[Todo.Existing] = F.delay{
        state = state.filterNot(_.id == todo.id) :+ todo

        todo
      }.map(_ => todo)

      override def deleteMany(todos: Vector[Todo.Existing]): F[Unit] =
        F.delay{state = state.filterNot(todo => todos.map(_.id).contains(todo.id))}

      override def deleteAll: F[Unit] =
        F.delay{state = Vector.empty}
    }
  }
}
