package com.devinsideyou
package todo
package crud

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.implicits._

import java.time.format.DateTimeFormatter

object DependencyGraph {
  def dsl[F[_] : Sync] (
    pattern: DateTimeFormatter,
    console: Console[F],
    random: Random[F]
  ): F[Controller[F]] = {
    Ref.of(Vector.empty[Todo.Existing]).map { state =>
      Controller.dsl(
        pattern = pattern,
        persistenceService = TodoPersistenceService.dsl(
          gateway = InMemoryTodoRepository.dsl(
            state
          )
        ),
        console = FancyConsole.dsl(console),
        random = random
      )
    }
  }
}
