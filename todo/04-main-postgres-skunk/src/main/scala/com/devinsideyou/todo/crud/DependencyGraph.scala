package com.devinsideyou
package todo
package crud

import cats._
import cats.effect.concurrent.Ref
import cats.implicits._

import java.time.format.DateTimeFormatter

object DependencyGraph {
  def dsl[F[_] : effect.Sync] (
    pattern: DateTimeFormatter,
    console: Console[F],
    random: Random[F],
    resource: effect.Resource[F, skunk.Session[F]]
  ): F[Controller[F]] = {
    PostgresRepository.dsl(resource).map { repository =>
      Controller.dsl(
        pattern = pattern,
        persistenceService = TodoPersistenceService.dsl(repository),
        console = FancyConsole.dsl(console),
        random = random
      )
    }
  }
}
