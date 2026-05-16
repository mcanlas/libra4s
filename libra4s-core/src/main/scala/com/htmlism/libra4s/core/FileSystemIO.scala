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

  def readString(path: Path): IO[String] =
    IO.blocking:
      Files.readString(path)

  def exists(path: Path): IO[Boolean] =
    IO.blocking:
      Files.exists(path)

  def resolve(path: Path, child: String): IO[Path] =
    IO.blocking:
      path.resolve(child)

  def findClassFiles(path: Path): IO[List[Path]] =
    findChildrenBySuffix(path, ".class")

  private def findChildrenBySuffix(path: Path, suffix: String) =
    Resource
      .fromAutoCloseable:
        IO.blocking:
          Files.walk(path)
      .use: stream =>
        IO.blocking:
          stream
            .iterator
            .asScala
            .filter(Files.isRegularFile(_))
            .filter(_.getFileName.toString.endsWith(suffix))
            .toList
            .sortBy(_.toString)
