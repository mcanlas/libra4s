package com.htmlism.libra4s.core

import weaver.SimpleIOSuite

object FileSystemIOSpec extends SimpleIOSuite:

  test("findClassFiles discovers class files recursively"):
    val scalaSource =
      """package foo.bar
        |
        |case class Dog(name: String)
        |""".stripMargin

    for
      compileResult <- ScalaCompiler
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
