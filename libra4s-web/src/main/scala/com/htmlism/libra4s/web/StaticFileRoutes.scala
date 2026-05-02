package com.htmlism.libra4s.web

import cats.effect.IO
import org.http4s.HttpRoutes
import org.http4s.StaticFile
import org.http4s.dsl.io.*

object StaticFileRoutes:
  private val allowedExtensions = List(".css", ".js")

  val routes: HttpRoutes[IO] =
    HttpRoutes.of[IO]:
      case req @ GET -> Root / path if allowedExtensions.exists(path.endsWith) =>
        StaticFile
          .fromResource[IO](s"/$path", Some(req))
          .getOrElseF(NotFound())
