package com.htmlism.libra4s.core

import cats.data.NonEmptyList
import weaver.SimpleIOSuite

object ProcessRunnerSpec extends SimpleIOSuite:

  test("runs a simple shell command"):
    val echoCmd = NonEmptyList.of("echo", "hello world")

    for result <- ProcessRunner
        .run(echoCmd)
    yield result match
      case Left(err) =>
        failure(s"process failed with exit code ${err.exitCode}")
      case Right(lines) =>
        val output = lines.mkString("\n").trim

        expect(lines.nonEmpty) &&
        expect(output.contains("hello world"))

  test("runs javap command"):
    for result <- ProcessRunner
        .run(NonEmptyList.of("javap", "-version"))
    yield result match
      case Left(err)    => failure(s"javap failed with exit code ${err.exitCode}")
      case Right(lines) => expect(lines.nonEmpty)
