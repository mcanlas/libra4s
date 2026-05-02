package com.htmlism.libra4s.web

import cats.data.*
import cats.effect.IO
import io.circe.generic.auto.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.io.*
import org.http4s.scalatags.*
import org.http4s.{scalatags as _, *}

import com.htmlism.libra4s.core.ProcessRunner

object DecompilerRoutes:
  private case class CompileRequest(code: String)

  lazy val routes: HttpRoutes[IO] =
    HttpRoutes.of[IO]:
      case GET -> Root =>
        Ok(DecompilerRoutesHtml.index)

      case req @ POST -> Root / "compile" =>
        req
          .attemptAs[CompileRequest]
          .leftSemiflatMap(_ => BadRequest("Invalid JSON body"))
          .semiflatMap: compileReq =>
            for
              xs <- ProcessRunner.run(NonEmptyList.of("ls", "-l", "/tmp"))

              _ <- IO.println(s"Got code: ${compileReq.code}")

              res <- Ok(xs.mkString("\n"))
            yield res
          .merge
