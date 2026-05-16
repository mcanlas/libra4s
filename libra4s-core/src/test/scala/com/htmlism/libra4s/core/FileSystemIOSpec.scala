package com.htmlism.libra4s.core

import cats.effect.*
import weaver.*

object FileSystemIOSpec extends IOSuite:
  type Res = ScalaCompiler

  def sharedResource: Resource[IO, ScalaCompiler] =
    ScalaCompilerTestSupport
      .sharedResource(ignore("install scalac and ensure it is on PATH to run scalac-dependent tests"))

  test("findClassFiles discovers class files recursively"): scalaCompiler =>
    val scalaSource =
      """package foo.bar
        |
        |case class Dog(name: String)
        |""".stripMargin
    for
      compileResult <- scalaCompiler
        .compileCode(scalaSource)

      result <- compileResult match
        case Left(err) =>
          IO.pure(failure(s"compilation failed with exit code ${err.exitCode}"))

        case Right((tempDir, _)) =>
          FileSystemIO
            .findClassFiles(tempDir)
            .map: classFiles =>
              val hasNestedPackageClass =
                classFiles.exists: path =>
                  val p = path.toString

                  p.contains("foo/bar") || p.contains("foo\\bar")

              expect(hasNestedPackageClass)
    yield result
