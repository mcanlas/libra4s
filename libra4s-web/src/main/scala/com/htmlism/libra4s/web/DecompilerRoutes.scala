package com.htmlism.libra4s.web

import cats.effect.IO
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
              tempDir <- FileSystemIO
                .createTempDirectory("libra4s-compile")

              scalaFilePath <- FileSystemIO
                .resolve(tempDir, "Input.scala")

              _ <- FileSystemIO
                .writeString(scalaFilePath, compileReq.code)

              phasesResult <- ScalaCompiler
                .runWithPhases(scalaFilePath.toString, tempDir.toString)

              res <- phasesResult match
                case Left(err) =>
                  Ok(
                    ApiResponse
                      .Success(CompileAndDisassembleResponse.fromCompileFailure(err)): CompileApiResponse
                  )

                case Right(phases) =>
                  for
                    classFilePath <- FileSystemIO
                      .findFirstChildBySuffix(tempDir, ".class")

                    javapResult <- classFilePath match
                      case Some(path) =>
                        JavaDisassembler
                          .run(path.toString)

                      case None =>
                        IO.pure(Right(List.empty[String]))

                    response = classFilePath match
                      case Some(_) =>
                        CompileAndDisassembleResponse.fromCompileSuccess(phases, javapResult)

                      case None =>
                        CompileAndDisassembleResponse.fromCompileSuccessWithoutClass(phases)

                    res <- Ok(
                      ApiResponse
                        .Success(response): CompileApiResponse
                    )
                  yield res
            yield res
          .merge
