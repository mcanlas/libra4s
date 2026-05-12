package com.htmlism.libra4s.core

import cats.effect.*
import cats.syntax.all.*

object ScalaCompilerTestSupport:
  def sharedResource(
      ignoreAction: IO[Unit]
  ): Resource[IO, ScalaCompiler] =
    Resource.eval:
      for
        maybeCompilerAttempt <- ScalaCompiler
          .build
          .attempt

        res <- maybeCompilerAttempt match
          case Left(err) =>
            ignoreAction *>
              IO.raiseError(err)

          case Right(compiler) =>
            compiler.pure[IO]
      yield res
