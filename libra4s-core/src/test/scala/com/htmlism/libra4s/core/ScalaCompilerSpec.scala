package com.htmlism.libra4s.core

import java.nio.file.Files

import cats.effect.*
import weaver.SimpleIOSuite

object ScalaCompilerSpec extends SimpleIOSuite:

  test("parseCompilerPhases extracts phase hints and groups content"):
    val scalaSource = """case class Dog(n: String)"""

    for
      tempFilePath <- TempFileFactory.createTempFile("dog", ".scala")
      _            <- IO.blocking(Files.writeString(tempFilePath, scalaSource))

      phases <- ScalaCompiler.runWithPhases(tempFilePath.toString)
    yield
      val phaseHints = phases.map(_.hint)

      expect(phaseHints.nonEmpty) &&
      expect(phaseHints.contains("syntax trees at end of                    parser")) &&
      expect(phaseHints.contains("syntax trees at end of                     typer")) &&
      expect(phases.headOption.exists(_.lines.nonEmpty)) &&
      expect(phases.forall(p => p.hint.nonEmpty))
