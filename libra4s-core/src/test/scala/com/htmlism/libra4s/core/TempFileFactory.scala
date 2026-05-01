package com.htmlism.libra4s.core

import java.nio.file.*

import cats.effect.*

object TempFileFactory:
  def createTempFile(prefix: String, suffix: String): IO[Path] =
    for
      path <- IO.blocking(Files.createTempFile(s"$prefix-", suffix))

      _ <- IO.println(s"Created temp file at $path")
    yield path
