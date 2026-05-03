package com.htmlism.libra4s.web

import java.nio.file.Files
import java.nio.file.Path

import scala.jdk.CollectionConverters.*

import cats.effect.IO
import cats.effect.Resource
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.io.*
import org.http4s.scalatags.*
import org.http4s.{scalatags as _, *}

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
              tempDir <- IO.blocking(Files.createTempDirectory("libra4s-compile-"))

              scalaFilePath <- IO.blocking:
                val path = tempDir.resolve("Input.scala")

                Files.writeString(path, compileReq.code)

                path

              phasesResult <- ScalaCompiler.runWithPhases(scalaFilePath.toString, tempDir.toString)

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
                    classFilePath <- findFirstClassFile(tempDir)

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

  // scalac can emit multiple files; for this POC we pick one .class path to feed javap.
  private def findFirstClassFile(tempDir: Path): IO[Option[Path]] =
    Resource
      .fromAutoCloseable(IO.blocking(Files.list(tempDir)))
      .use: stream =>
        IO.blocking(stream.iterator.asScala.find(_.getFileName.toString.endsWith(".class")))
