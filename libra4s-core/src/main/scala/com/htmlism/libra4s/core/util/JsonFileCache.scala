package com.htmlism.libra4s.core.util

import java.nio.file.Files

import cats.effect.IO
import cats.effect.Resource
import io.circe.Decoder
import io.circe.Encoder

trait JsonFileCache:
  def get[A](a: A)(using Cacheable[A], Decoder[A], Encoder[A]): IO[Option[A]]

  def write[A](a: A)(using Cacheable[A], Decoder[A], Encoder[A]): IO[A]

object JsonFileCache:
  def temporary: Resource[IO, JsonFileCache] =
    temporary("libra4s-cache")

  def temporary(prefix: String): Resource[IO, JsonFileCache] =
    Resource
      .eval:
        IO
          .blocking:
            Files.createTempDirectory(s"$prefix-")
          .map(TemporaryJsonFileCache(_))
