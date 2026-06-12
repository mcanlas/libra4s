package com.htmlism.libra4s.core

import cats.effect.*
import weaver.*

import com.htmlism.rufio.cats.io.syntax.*
import com.htmlism.rufio.core.Path

object JavaDisassemblerSpec extends IOSuite:
  type Res = (ScalaCompiler, JavaDisassembler)

  def sharedResource: Resource[IO, (ScalaCompiler, JavaDisassembler)] =
    for
      scalaCompiler <- ScalaCompilerTestSupport
        .sharedResource(ignore("install scalac and ensure it is on PATH to run scalac-dependent tests"))

      javaDisassembler <- JavaDisassemblerTestSupport
        .sharedResource(ignore("install javap and ensure it is on PATH to run javap-dependent tests"))
    yield (scalaCompiler, javaDisassembler)

  test("disassembles a compiled class file"): r =>
    val (scalaCompiler, javaDisassembler) = r

    val scalaSource = """case class Cat(name: String)"""

    for
      tempDirectory <- Path.createTemporaryDirectory

      tempScalaPath = tempDirectory
        .resolve("Cat.scala")

      _ <- tempScalaPath
        .writeString(scalaSource)
      // Compile the Scala file
      compileResult <- scalaCompiler
        .run(tempScalaPath.toString)

      // The compiler outputs to /tmp by default, so the class should be at /tmp/Cat.class
      classFilePath = "/tmp/Cat.class"

      // Disassemble the class
      disassemblyResult <- javaDisassembler
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
