package com.htmlism.libra4s.core

import cats.data.NonEmptyList
import cats.effect.*

object ScalaCompiler:
  final case class Phase(hint: String, lines: List[String])

  def runWithPhases(
      scalaFilePath: String
  ): IO[List[Phase]] =
    runWithPhases(scalaFilePath, "/tmp")

  def runWithPhases(
      scalaFilePath: String,
      outputDirectory: String
  ): IO[List[Phase]] =
    for lines <- run(scalaFilePath, outputDirectory)
    yield parseCompilerPhases(lines)

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
          case None => phases
      else phases
    }

  def run(
      scalaFilePath: String
  ): IO[List[String]] =
    run(scalaFilePath, "/tmp")

  def run(
      scalaFilePath: String,
      outputDirectory: String
  ): IO[List[String]] =
    ProcessRunner.run(
      NonEmptyList.of(
        "scalac",
        "-Vprint:all",
        "-color:never",
        "-d",
        outputDirectory,
        scalaFilePath
      )
    )
