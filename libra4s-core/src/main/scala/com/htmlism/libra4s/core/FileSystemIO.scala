package com.htmlism.libra4s.core

import java.nio.file.Files
import java.nio.file.Path

import scala.jdk.CollectionConverters.*

import cats.effect.*

object FileSystemIO:
  def createTempFile(prefix: String, suffix: String): IO[Path] =
    IO.blocking:
      Files.createTempFile(s"$prefix-", suffix)

  def createTempDirectory(prefix: String): IO[Path] =
    IO.blocking:
      Files.createTempDirectory(s"$prefix-")

  def writeString(path: Path, content: String): IO[Path] =
    IO.blocking:
      Files.writeString(path, content)

  def resolve(path: Path, child: String): IO[Path] =
    IO.blocking:
      path.resolve(child)

  def findChildrenBySuffix(path: Path, suffix: String): IO[List[Path]] =
    Resource
      .fromAutoCloseable:
        IO.blocking:
          Files.list(path)
      .use: stream =>
        IO.blocking:
          stream
            .iterator
            .asScala
            .filter(_.getFileName.toString.endsWith(suffix))
            .toList
            .sortBy(_.getFileName.toString)
