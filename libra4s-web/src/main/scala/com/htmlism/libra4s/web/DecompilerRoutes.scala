package com.htmlism.libra4s.web

import cats.data.*
import cats.effect.IO
import org.http4s.dsl.io.*
import org.http4s.scalatags.*
import org.http4s.{scalatags as _, *}

import com.htmlism.libra4s.core.ProcessRunner

object DecompilerRoutes:
  val routes: HttpRoutes[IO] =
    HttpRoutes.of[IO]:
      case GET -> Root =>
        Ok(DecompilerRoutesHtml.index)

      case POST -> Root / "list" =>
        for
          xs <- ProcessRunner.run(NonEmptyList.of("ls", "-l", "/tmp"))

          res <- Ok(xs.mkString("\n"))
        yield res
