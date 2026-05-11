package com.htmlism.libra4s.core

import cats.effect.*
import cats.effect.std.Env
import cats.syntax.all.*
import weaver.*

object ScalaCompilerSpec extends IOSuite:
  type Res = Boolean

  def sharedResource: Resource[IO, Boolean] =
    Resource.eval(
      Env
        .make[IO]
        .get("HAS_SCALAC")
        .map(_.contains("true"))
        .flatTap(ignore("set HAS_SCALAC=true to run scalac-dependent tests").unlessA(_))
    )

  test("parseCompilerPhases extracts phase hints and groups content"): _ =>
    val scalaSource = """case class Dog(n: String)"""

    for
      tempFilePath <- FileSystemIO
        .createTempFile("dog", ".scala")
      _ <- FileSystemIO
        .writeString(tempFilePath, scalaSource)

      phasesResult <- ScalaCompiler
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
