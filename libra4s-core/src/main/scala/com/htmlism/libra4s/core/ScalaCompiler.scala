package com.htmlism.libra4s.core

import java.nio.file.Path

import cats.data.NonEmptyList
import cats.effect.*

final case class ScalaCompiler private (command: String):
  def compileCode(
      code: String
  ): IO[(Path, Either[ProcessRunner.ProcessRunnerError, List[ScalaCompiler.Phase]])] =
    for
      tempDir <- FileSystemIO
        .createTempDirectory("libra4s-compile")

      scalaFilePath <- FileSystemIO
        .resolve(tempDir, "Input.scala")

      _ <- FileSystemIO
        .writeString(scalaFilePath, code)

      phasesResult <- runWithPhases(scalaFilePath.toString, tempDir.toString)
    yield (tempDir, phasesResult)

  def runWithPhases(
      scalaFilePath: String
  ): IO[Either[ProcessRunner.ProcessRunnerError, List[ScalaCompiler.Phase]]] =
    runWithPhases(scalaFilePath, "/tmp")

  def runWithPhases(
      scalaFilePath: String,
      outputDirectory: String
  ): IO[Either[ProcessRunner.ProcessRunnerError, List[ScalaCompiler.Phase]]] =
    run(scalaFilePath, outputDirectory).map(_.map(ScalaCompiler.parseCompilerPhases))

  def run(
      scalaFilePath: String
  ): IO[Either[ProcessRunner.ProcessRunnerError, List[String]]] =
    run(scalaFilePath, "/tmp")

  def run(
      scalaFilePath: String,
      outputDirectory: String
  ): IO[Either[ProcessRunner.ProcessRunnerError, List[String]]] =
    ProcessRunner.run(
      NonEmptyList.of(
        command,
        "-Vprint:all",
        "-color:never",
        "-d",
        outputDirectory,
        scalaFilePath
      )
    )

object ScalaCompiler:
  final case class Phase(hint: String, lines: List[String])

  def build: IO[ScalaCompiler] =
    ProcessRunner
      .run(NonEmptyList.of("which", "scalac"))
      .flatMap:
        case Right(_) =>
          IO.pure(ScalaCompiler("scalac"))

        case Left(_) =>
          IO.raiseError(RuntimeException("scalac is unavailable on PATH"))

  private def parseCompilerPhases(
      lines: List[String]
  ): List[Phase] =
    lines.foldLeft(List.empty[Phase]) { (phases, line) =>
      if line.startsWith("[[syntax trees at end of") then
        // Extract hint from line: [[...]] // /path
        val hintStart = line.indexOf("[[") + 2
        val hintEnd   = line.indexOf("]]")
        val hint      = line.substring(hintStart, hintEnd).trim
        // Add new phase with empty lines (will be populated by following lines)
        // Handles: normal phases, "unchanged since" phases, and "MegaPhase" entries
        phases :+ Phase(hint, List.empty)
      else if phases.nonEmpty then
        // Append this line to the last phase
        phases.lastOption match
          case Some(lastPhase) =>
            val updatedPhase = lastPhase.copy(lines = lastPhase.lines :+ line)
            phases.dropRight(1) :+ updatedPhase

          case None =>
            phases
      else phases
    }
