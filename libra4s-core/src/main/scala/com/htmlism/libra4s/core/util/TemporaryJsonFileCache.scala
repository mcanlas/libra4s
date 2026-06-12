package com.htmlism.libra4s.core.util

import java.nio.file.Path

import cats.effect.IO
import io.circe.Decoder
import io.circe.Encoder
import io.circe.parser.decode
import io.circe.syntax.*

import com.htmlism.rufio.cats.io.syntax.*

final class TemporaryJsonFileCache(root: Path) extends JsonFileCache:
  def get[A](a: A)(using Cacheable[A], Decoder[A], Encoder[A]): IO[Option[A]] =
    val path =
      pathFor(a)

    for
      exists <- path.exists

      payload <- if exists then readPayload[A](path).map(Some(_)) else IO.pure(None)
    yield payload

  def write[A](a: A)(using Cacheable[A], Decoder[A], Encoder[A]): IO[A] =
    for _ <- pathFor(a)
        .writeString(a.asJson.noSpaces)
    yield a

  private def pathFor[A](a: A)(using Cacheable[A]) =
    root
      .resolve(s"${Cacheable.slug(a)}.json")

  private def readPayload[A](path: Path)(using Decoder[A]) =
    for
      json <- path.readString

      payload <- IO
        .fromEither:
          decode[A](json)
    yield payload
