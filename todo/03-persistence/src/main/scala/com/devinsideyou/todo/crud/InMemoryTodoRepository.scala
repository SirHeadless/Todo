package com.devinsideyou
package todo
package crud

import cats._
import cats.implicits._
import cats._
import cats.effect.concurrent.Ref

object InMemoryTodoRepository {
  def dsl[F[_] : effect.Sync](state: Ref[F, Vector[Todo.Existing]]): TodoRepository[F] = {
    new TodoRepository[F] {

      private val statement = Statement.dsl(state)

      override def writeMany(todos: Vector[Todo]): F[Vector[Todo.Existing]] = {
        todos.traverse(writeOne)
      }

      private def writeOne(todo: Todo): F[Todo.Existing] =
        todo match {
          case data: Todo.Data => statement.insertOne(data)
          case existing: Todo.Existing => statement.updateOne(existing)
        }


      override def readManyById(ids: Vector[String]): F[Vector[Todo.Existing]] =
        statement.selectAll.map(_.filter(todos => ids.contains(todos.id)))

      override def readManyByPartialDescription(partialDescription: String): F[Vector[Todo.Existing]] =
        statement.selectAll.map {_.filter(_.description.toLowerCase.contains(partialDescription.toLowerCase)
          )
      }

      override def readAll: F[Vector[Todo.Existing]] =
        statement.selectAll

      private def updateOne(todo: Todo.Existing): F[Todo.Existing] =
        statement.updateOne(todo)

      override def deleteMany(todos: Vector[Todo.Existing]): F[Unit] =
        statement.deleteMany(todos)

      override def deleteAll: F[Unit] =
        statement.deleteAll
    }
  }
}
