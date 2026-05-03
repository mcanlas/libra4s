package com.htmlism.libra4s.core

import scala.sys.process.*

import cats.data.NonEmptyList
import cats.effect.*
import cats.syntax.all.*

object ProcessRunner:
  final case class ProcessRunnerError(exitCode: Int, stdout: List[String], stderr: List[String])

  private class ListProcessLogger extends ProcessLogger:
    private val stdoutBuilder = List.newBuilder[String]
    private val stderrBuilder = List.newBuilder[String]

    def out(s: => String): Unit = stdoutBuilder += s
    def err(s: => String): Unit = stderrBuilder += s
    def buffer[A](f: => A): A   = f

    def getStdout: IO[List[String]] = IO(stdoutBuilder.result())
    def getStderr: IO[List[String]] = IO(stderrBuilder.result())

  def run(
      args: NonEmptyList[String]
  ): IO[Either[ProcessRunnerError, List[String]]] =
    for
      logger = new ListProcessLogger

      process <- IO
        .blocking:
          args.toList.run(logger, connectInput = false)

      exitCode <- IO
        .blocking:
          process.exitValue()

      res <-
        if exitCode == 0 then logger.getStdout.map(_.asRight)
        else
          for
            stdout <- logger.getStdout
            stderr <- logger.getStderr
          yield ProcessRunnerError(exitCode, stdout, stderr).asLeft
    yield res
