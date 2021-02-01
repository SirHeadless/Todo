package com.devinsideyou
package todo
package crud

import cats.effect.Sync

import java.time.format.DateTimeFormatter

object DependencyGraph {
  def dsl[F[_] : Sync] (
    pattern: DateTimeFormatter,
    console: Console[F],
    random: Random[F]
  ): Controller[F] =
    Controller.dsl(
      pattern = pattern,
      persistenceService = TodoPersistenceService.dsl(
        gateway = InMemoryTodoRepository.dsl
      ),
      console = FancyConsole.dsl(console),
      random = random
    )
}
