package com.devinsideyou.todo

import cats._

import java.util.UUID

trait UUIDGenerator[F[_]] {
  def genUUID: F[UUID]
}

object UUIDGenerator {
  def dsl[F[_] : effect.Sync]: F[UUIDGenerator[F]] =
    F.delay {
      new UUIDGenerator[F] {
        override def genUUID: F[UUID] = F.delay {
          UUID.randomUUID()
        }
      }
    }
}