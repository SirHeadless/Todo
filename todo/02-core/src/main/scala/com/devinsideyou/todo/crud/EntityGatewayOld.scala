package com.devinsideyou.todo.crud

import com.devinsideyou.Todo

trait EntityGatewayOld {
  def writeMany(todos: Vector[Todo]): Vector[Todo.Existing]

  def readManyById(ids: Vector[String]): Vector[Todo.Existing]
  def readManyByPartialDescription(partialDescription: String): Vector[Todo.Existing]
  def readAll: Vector[Todo.Existing]

  def deleteMany(todos: Vector[Todo.Existing]): Unit
  def deleteAll: Unit
}


