package com.htmlism.libra4s.core

import cats.effect.*
import cats.syntax.all.*

object JavaDisassemblerTestSupport:
  def sharedResource(
      ignoreAction: IO[Unit]
  ): Resource[IO, JavaDisassembler] =
    Resource.eval:
      for
        maybeDisassemblerAttempt <- JavaDisassembler
          .build
          .attempt

        res <- maybeDisassemblerAttempt match
          case Left(err) =>
            ignoreAction *>
              IO.raiseError(err)

          case Right(disassembler) =>
            disassembler.pure[IO]
      yield res
