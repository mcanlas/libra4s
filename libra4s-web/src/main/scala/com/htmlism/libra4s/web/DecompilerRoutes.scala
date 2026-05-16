package com.htmlism.libra4s.web

import cats.data.EitherT
import cats.effect.IO
import cats.syntax.all.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.io.*
import org.http4s.scalatags.*
import org.http4s.{scalatags as _, *}

import com.htmlism.libra4s.core.FileSystemIO
import com.htmlism.libra4s.core.JavaDisassembler
import com.htmlism.libra4s.core.ScalaCompiler

object DecompilerRoutes:
  private type CompileApiResponse = ApiResponse[String, CompileAndDisassembleResponse]

  def routes(
      scalaCompiler: ScalaCompiler,
      javaDisassembler: JavaDisassembler
  ): HttpRoutes[IO] =
    HttpRoutes.of[IO]:
      case GET -> Root =>
        Ok(DecompilerRoutesHtml.index)

      case req @ POST -> Root / "compile" =>
        req
          .attemptAs[CompileRequest]
          .leftSemiflatMap(_ => BadRequest(ApiResponse.Failure("Invalid JSON body"): CompileApiResponse))
          .flatMap: compileReq =>
            EitherT:
              scalaCompiler
                .compileCode(compileReq.code)
            .leftSemiflatMap: err =>
              Ok(
                ApiResponse
                  .Success(CompileAndDisassembleResponse.fromCompileFailure(err)): CompileApiResponse
              )
          .semiflatMap: (tempDir, phases) =>
            for
              classFiles <- FileSystemIO
                .findClassFiles(tempDir)

              response <-
                if classFiles.nonEmpty then
                  classFiles
                    .traverse: path =>
                      javaDisassembler
                        .run(path.toString)
                        .map(result => path.getFileName.toString -> result)
                    .map(results => CompileAndDisassembleResponse.fromCompileSuccess(phases, results))
                else
                  IO.pure:
                    CompileAndDisassembleResponse.fromCompileSuccessWithoutClass(phases)

              res <- Ok(
                ApiResponse
                  .Success(response): CompileApiResponse
              )
            yield res
          .merge
