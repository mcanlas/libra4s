package com.htmlism.libra4s.web

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

  lazy val routes: HttpRoutes[IO] =
    HttpRoutes.of[IO]:
      case GET -> Root =>
        Ok(DecompilerRoutesHtml.index)

      case req @ POST -> Root / "compile" =>
        req
          .attemptAs[CompileRequest]
          .leftSemiflatMap(_ => BadRequest(ApiResponse.Failure("Invalid JSON body"): CompileApiResponse))
          .semiflatMap: compileReq =>
            for
              compileResult <- ScalaCompiler
                .compileCode(compileReq.code)

              res <- compileResult match
                case (_, Left(err)) =>
                  Ok(
                    ApiResponse
                      .Success(CompileAndDisassembleResponse.fromCompileFailure(err)): CompileApiResponse
                  )

                case (tempDir, Right(phases)) =>
                  for
                    classFiles <- FileSystemIO
                      .findClassFiles(tempDir)

                    response <-
                      if classFiles.nonEmpty then
                        classFiles
                          .traverse: path =>
                            JavaDisassembler
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
            yield res
          .merge
