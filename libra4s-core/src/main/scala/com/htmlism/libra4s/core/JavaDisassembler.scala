package com.htmlism.libra4s.core

import cats.data.NonEmptyList
import cats.effect.*

object JavaDisassembler:
  def run(
      classFilePath: String
  ): IO[List[String]] =
    ProcessRunner.run(
      NonEmptyList.of(
        "javap",
//        "-c",
        classFilePath
      )
    )
