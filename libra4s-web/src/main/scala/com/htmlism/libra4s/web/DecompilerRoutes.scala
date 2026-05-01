package com.htmlism.libra4s.web

import cats.effect.IO
import org.http4s.*
import org.http4s.dsl.io.*

object DecompilerRoutes:
  val routes: HttpRoutes[IO] =
    HttpRoutes.of[IO]:
      case GET -> Root =>
        for res <- Ok("Decompiler endpoint is up")
        yield res
