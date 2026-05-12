package com.htmlism.libra4s.core

import cats.effect.*
import weaver.*

object ScalaCompilerSpec extends IOSuite:
  type Res = ScalaCompiler

  def sharedResource: Resource[IO, ScalaCompiler] =
    ScalaCompilerTestSupport
      .sharedResource(ignore("install scalac and ensure it is on PATH to run scalac-dependent tests"))

  test("parseCompilerPhases extracts phase hints and groups content"): scalaCompiler =>
    val scalaSource = """case class Dog(n: String)"""

    for
      tempFilePath <- FileSystemIO
        .createTempFile("dog", ".scala")
      _ <- FileSystemIO
        .writeString(tempFilePath, scalaSource)

      phasesResult <- scalaCompiler
        .runWithPhases(tempFilePath.toString)
    yield phasesResult match
      case Left(err) =>
        failure(s"compilation failed with exit code ${err.exitCode}")

      case Right(phases) =>
        val phaseHints = phases.map(_.hint)

        expect(phaseHints.nonEmpty) &&
        expect(phaseHints.contains("syntax trees at end of                    parser")) &&
        expect(phaseHints.contains("syntax trees at end of                     typer")) &&
        expect(phases.headOption.exists(_.lines.nonEmpty)) &&
        expect(phases.forall(p => p.hint.nonEmpty))
