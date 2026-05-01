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
      disassemblyLines <- JavaDisassembler.run(classFilePath)
    yield
      val output = disassemblyLines.mkString("\n")

      expect(disassemblyLines.nonEmpty) &&
      expect(output.contains("Cat")) &&
      expect(output.contains("public") || output.contains("class"))
