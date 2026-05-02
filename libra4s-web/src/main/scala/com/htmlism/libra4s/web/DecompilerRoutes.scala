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
              tempDir <- IO.blocking(Files.createTempDirectory("libra4s-compile-"))

              scalaFilePath <- IO.blocking:
                val path = tempDir.resolve("Input.scala")

                Files.writeString(path, compileReq.code)

                path

              compilerPhases <- ScalaCompiler.runWithPhases(scalaFilePath.toString, tempDir.toString)

              classFilePath <- findFirstClassFile(tempDir)

              disassemblyLines <- classFilePath match
                case Some(path) => JavaDisassembler.run(path.toString)
                case None       => IO.pure(List("No class files generated"))

              res <- Ok(CompileAndDisassembleResponse.from(compilerPhases, disassemblyLines))
            yield res
          .merge

  // scalac can emit multiple files; for this POC we pick one .class path to feed javap.
  private def findFirstClassFile(tempDir: Path): IO[Option[Path]] =
    Resource
      .fromAutoCloseable(IO.blocking(Files.list(tempDir)))
      .use: stream =>
        IO.blocking(stream.iterator.asScala.find(_.getFileName.toString.endsWith(".class")))
