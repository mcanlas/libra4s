package com.htmlism.libra4s.core

import cats.effect.*
import weaver.*

object JavaDisassemblerSpec extends IOSuite:
  type Res = ScalaCompiler

  def sharedResource: Resource[IO, ScalaCompiler] =
    ScalaCompilerTestSupport
      .sharedResource(ignore("install scalac and ensure it is on PATH to run scalac-dependent tests"))

  test("disassembles a compiled class file"): scalaCompiler =>
    val scalaSource = """case class Cat(name: String)"""
    for
      tempScalaPath <- FileSystemIO
        .createTempFile("cat", ".scala")
      _ <- FileSystemIO
        .writeString(tempScalaPath, scalaSource)
      // Compile the Scala file
      compileResult <- scalaCompiler
        .run(tempScalaPath.toString)

      // The compiler outputs to /tmp by default, so the class should be at /tmp/Cat.class
      classFilePath = "/tmp/Cat.class"

      // Disassemble the class
      disassemblyResult <- JavaDisassembler
        .run(classFilePath)
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
