package com.devinsideyou.todo.crud

import cats._
import cats._
import cats.implicits._
import cats.effect.concurrent.Ref
import com.devinsideyou.Todo

trait Statement[F[_]] {
  def insertOne(data: Todo.Data): F[Todo.Existing]

  def updateOne(todo: Todo.Existing): F[Todo.Existing]

  def selectAll: F[Vector[Todo.Existing]]

  def deleteMany(todos: Vector[Todo.Existing]): F[Unit]

  def deleteAll: F[Unit]
}

object Statement {

  def dsl[F[_] : Functor : FlatMap](state: Ref[F, Vector[Todo.Existing]]): Statement[F] =
    new Statement[F] {

      override def insertOne(data: Todo.Data): F[Todo.Existing] = {
        state.modify { a =>
          val newTodo = Todo.Existing(a.size.toString, data)
          (a :+ newTodo) -> newTodo
        }
      }

      override def updateOne(todo: Todo.Existing): F[Todo.Existing] = state.modify { s =>
        (s.filterNot(_.id === todo.id) :+ todo) -> todo
      }

      override def selectAll: F[Vector[Todo.Existing]] = state.get


      override def deleteMany(todos: Vector[Todo.Existing]): F[Unit] = state.update(_.filterNot(todo => todos.map(_.id).contains(todo.id)))

      override def deleteAll: F[Unit] =
        state.set(Vector.empty)
    }
}
