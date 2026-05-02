package com.htmlism.libra4s.core

import java.nio.file.Files

import cats.effect.*
import weaver.SimpleIOSuite

object JavaDisassemblerSpec extends SimpleIOSuite:

  test("disassembles a compiled class file"):
    val scalaSource = """case class Cat(name: String)"""

    for
      tempScalaPath <- TempFileFactory.createTempFile("cat", ".scala")
      _             <- IO.blocking(Files.writeString(tempScalaPath, scalaSource))

      // Compile the Scala file
      compileResult <- ScalaCompiler.run(tempScalaPath.toString)

      // The compiler outputs to /tmp by default, so the class should be at /tmp/Cat.class
      classFilePath = "/tmp/Cat.class"

      // Disassemble the class
      disassemblyResult <- JavaDisassembler.run(classFilePath)
    yield (compileResult, disassemblyResult) match
      case (Left(err), _) =>
        failure(s"compilation failed with exit code ${err.exitCode}")
      case (_, Left(err)) =>
        failure(s"disassembly failed with exit code ${err.exitCode}")
      case (Right(_), Right(lines)) =>
        val output = lines.mkString("\n")

        expect(lines.nonEmpty) &&
        expect(output.contains("Cat")) &&
        expect(output.contains("public") || output.contains("class"))
