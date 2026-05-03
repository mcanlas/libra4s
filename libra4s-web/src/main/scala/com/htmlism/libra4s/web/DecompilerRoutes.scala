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
                  InternalServerError(
                    ApiResponse
                      .Failure(
                        s"Compile error (exit ${err.exitCode}):\n${err.stderr.mkString("\n")}"
                      ): CompileApiResponse
                  )

                case Right(phases) =>
                  for
                    classFilePath <- FileSystemIO
                      .findFirstChildBySuffix(tempDir, ".class")

                    disassemblyLines <- classFilePath match
                      case Some(path) =>
                        JavaDisassembler
                          .run(path.toString)
                          .map(_.fold(e => e.stderr, identity))

                      case None => IO.pure(List("No class files generated"))

                    res <- Ok(
                      ApiResponse
                        .Success(CompileAndDisassembleResponse.from(phases, disassemblyLines)): CompileApiResponse
                    )
                  yield res
            yield res
          .merge
