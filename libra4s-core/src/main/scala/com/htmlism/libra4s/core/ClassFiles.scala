package com.htmlism.libra4s.core

import java.nio.file.Path

import cats.effect.IO
import cats.syntax.all.*

import com.htmlism.rufio.cats.io.syntax.*

object ClassFiles:
  def findUnder(path: Path): IO[List[Path]] =
    for
      children <- path.list

      descendants <- children
        .traverse: child =>
          for
            isDirectory <- child.isDirectory

            paths <-
              if isDirectory then findUnder(child)
              else List(child).pure[IO]
          yield paths
    yield descendants
      .flatten
      .filter(_.getFileName.toString.endsWith(".class"))
      .sortBy(_.toString)
