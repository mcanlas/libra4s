package com.htmlism.libra4s.core

import weaver.SimpleIOSuite

object ScalaCompilerSpec extends SimpleIOSuite:

  test("parseCompilerPhases extracts phase hints and groups content"):
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
