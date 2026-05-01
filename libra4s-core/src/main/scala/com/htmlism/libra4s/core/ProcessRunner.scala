package com.htmlism.libra4s.core

import scala.sys.process.*

import cats.data.NonEmptyList
import cats.effect.*

object ProcessRunner:
  def run(
      args: NonEmptyList[String]
  ): IO[List[String]] =
    IO.blocking:
      args.toList.!!
    .map(_.linesIterator.toList)
