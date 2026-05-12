package com.htmlism.libra4s.core

import cats.data.NonEmptyList
import cats.effect.*

final case class JavaDisassembler private (command: String):
  def run(
      classFilePath: String
  ): IO[Either[ProcessRunner.ProcessRunnerError, List[String]]] =
    ProcessRunner.run(
      NonEmptyList.of(
        command,
        "-c",
        classFilePath
      )
    )

object JavaDisassembler:
  def build: IO[JavaDisassembler] =
    ProcessRunner
      .run(NonEmptyList.of("which", "javap"))
      .flatMap:
        case Right(_) =>
          IO.pure(JavaDisassembler("javap"))

        case Left(_) =>
          IO.raiseError(RuntimeException("javap is unavailable on PATH"))
