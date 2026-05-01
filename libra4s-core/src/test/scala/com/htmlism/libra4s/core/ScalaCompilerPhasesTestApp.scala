package com.htmlism.libra4s.core

import cats.effect.*

object ScalaCompilerPhasesTestApp extends IOApp:
  def run(args: List[String]): IO[ExitCode] =
    args match
      case scalaFilePath :: Nil =>
        ScalaCompiler
          .runWithPhases(scalaFilePath)
          .flatMap { phases =>
            val output = phases
              .map { phase =>
                s"=== ${phase.hint} ===\n${phase.lines.mkString("\n")}"
              }
              .mkString("\n\n")
            IO.println(output)
          }
          .as(ExitCode.Success)
      case _ =>
        IO.println("Usage: ScalaCompilerPhasesTestApp <scala-file-path>")
          .as(ExitCode.Error)
