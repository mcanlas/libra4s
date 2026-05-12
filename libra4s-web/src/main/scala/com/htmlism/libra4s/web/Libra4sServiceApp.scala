package com.htmlism.libra4s.web

import cats.effect.*
import cats.syntax.all.*
import com.comcast.ip4s.*
import org.http4s.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.server.middleware.Logger

import com.htmlism.libra4s.core.JavaDisassembler
import com.htmlism.libra4s.core.ScalaCompiler

object Libra4sServiceApp extends ResourceApp.Forever:
  def run(args: List[String]): Resource[IO, Unit] =
    for
      _ <- Resource
        .eval(IO.println("Starting libra4s service..."))

      scalaCompiler <- Resource
        .eval(ScalaCompiler.build)

      javaDisassembler <- Resource
        .eval(JavaDisassembler.build)

      _ <- EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(
          Logger.httpApp(logHeaders = true, logBody = false)(
            (
              StaticFileRoutes.routes <+>
                DecompilerRoutes.routes(scalaCompiler, javaDisassembler)
            ).orNotFound
          )
        )
        .build
    yield ()
