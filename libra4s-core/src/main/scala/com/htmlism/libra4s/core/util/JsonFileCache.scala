package com.htmlism.libra4s.core.util

import cats.effect.IO
import cats.effect.Resource
import io.circe.Decoder
import io.circe.Encoder

import com.htmlism.libra4s.core.FileSystemIO

trait JsonFileCache:
  def get[A](a: A)(using Cacheable[A], Decoder[A], Encoder[A]): IO[Option[A]]

  def write[A](a: A)(using Cacheable[A], Decoder[A], Encoder[A]): IO[A]

object JsonFileCache:
  def temporary: Resource[IO, JsonFileCache] =
    temporary("libra4s-cache")

  def temporary(prefix: String): Resource[IO, JsonFileCache] =
    Resource
      .eval:
        FileSystemIO
          .createTempDirectory(prefix)
          .map(TemporaryJsonFileCache(_))
