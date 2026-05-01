package com.htmlism.libra4s.core

import cats.data.NonEmptyList
import weaver.SimpleIOSuite

object ProcessRunnerSpec extends SimpleIOSuite:

  test("runs a simple shell command"):
    val echoCmd = NonEmptyList.of("echo", "hello world")

    for result <- ProcessRunner.run(echoCmd)
    yield
      val output = result.mkString("\n").trim

      expect(result.nonEmpty) &&
      expect(output.contains("hello world"))

  test("runs javap command"):
    for result <- ProcessRunner.run(NonEmptyList.of("javap", "-version"))
    yield expect(result.nonEmpty)
