package com.htmlism.libra4s.core

import cats.effect.*
import weaver.*

object FileSystemIOSpec extends IOSuite:
  type Res = ScalaCompiler

  def sharedResource: Resource[IO, ScalaCompiler] =
    ScalaCompilerTestSupport
      .sharedResource(ignore("set HAS_SCALAC=true to run scalac-dependent tests"))

  test("findClassFiles discovers class files recursively"): scalaCompiler =>
    val scalaSource =
      """package foo.bar
        |
        |case class Dog(name: String)
        |""".stripMargin
    for
      compileResult <- scalaCompiler
        .compileCode(scalaSource)
      (tempDir, phasesResult) = compileResult

      classFiles <- FileSystemIO
        .findClassFiles(tempDir)
    yield phasesResult match
      case Left(err) =>
        failure(s"compilation failed with exit code ${err.exitCode}")

      case Right(_) =>
        val hasNestedPackageClass =
          classFiles.exists: path =>
            val p = path.toString

            p.contains("foo/bar") || p.contains("foo\\bar")

        expect(hasNestedPackageClass)
