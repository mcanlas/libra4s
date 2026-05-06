package com.htmlism.libra4s.web

import io.circe.Encoder
import io.circe.JsonObject
import io.circe.generic.semiauto.deriveEncoder
import io.circe.syntax.*

import com.htmlism.libra4s.core.ProcessRunner.ProcessRunnerError
import com.htmlism.libra4s.core.ScalaCompiler

final case class ProcessErrorResponse(exitCode: Int, lines: List[String])

object ProcessErrorResponse:
  given Encoder[ProcessErrorResponse] = deriveEncoder

enum StageAttempt[+E, +A]:
  case Success(value: A) extends StageAttempt[Nothing, A]
  case Failure(error: E) extends StageAttempt[E, Nothing]

object StageAttempt:
  given [E: Encoder, A: Encoder]: Encoder.AsObject[StageAttempt[E, A]] =
    Encoder
      .AsObject
      .instance:
        case Success(value) =>
          JsonObject(
            "state" -> "success".asJson,
            "value" -> value.asJson
          )

        case Failure(error) =>
          JsonObject(
            "state" -> "failure".asJson,
            "error" -> error.asJson
          )

final case class JavapErrorResponse(errors: List[ProcessErrorResponse])

object JavapErrorResponse:
  given Encoder[JavapErrorResponse] = deriveEncoder

final case class CompileAndDisassembleResponse(
    compiler: StageAttempt[ProcessErrorResponse, CompilerResponse],
    javap: StageAttempt[JavapErrorResponse, JavapResponse]
)

object CompileAndDisassembleResponse:
  def fromCompileFailure(error: ProcessRunnerError): CompileAndDisassembleResponse =
    CompileAndDisassembleResponse(
      compiler = StageAttempt.Failure(toProcessErrorResponse(error)),
      javap    = StageAttempt.Failure(
        JavapErrorResponse(List(ProcessErrorResponse(error.exitCode, List("Compilation failed"))))
      )
    )

  def fromCompileSuccess(
      compilerPhases: List[ScalaCompiler.Phase],
      javapResults: List[(String, Either[ProcessRunnerError, List[String]])]
  ): CompileAndDisassembleResponse =
    CompileAndDisassembleResponse(
      compiler = StageAttempt.Success(toCompilerResponse(compilerPhases)),
      javap    = toAggregatedJavapAttempt(javapResults)
    )

  def fromCompileSuccessWithoutClass(compilerPhases: List[ScalaCompiler.Phase]): CompileAndDisassembleResponse =
    CompileAndDisassembleResponse(
      compiler = StageAttempt.Success(toCompilerResponse(compilerPhases)),
      javap    = StageAttempt.Failure(
        JavapErrorResponse(List(ProcessErrorResponse(0, List("No class files generated"))))
      )
    )

  private def toProcessErrorResponse(error: ProcessRunnerError): ProcessErrorResponse =
    val lines = if error.stderr.nonEmpty then error.stderr else error.stdout
    ProcessErrorResponse(error.exitCode, lines)

  private def toAggregatedJavapAttempt(
      javapResults: List[(String, Either[ProcessRunnerError, List[String]])]
  ): StageAttempt[JavapErrorResponse, JavapResponse] =
    val failures: List[(String, ProcessErrorResponse)] =
      javapResults.collect:
        case (classFile, Left(error)) =>
          classFile -> toProcessErrorResponse(error)

    failures match
      case _ :: _ =>
        val errors =
          failures.map: (classFile, error) =>
            error.copy(lines = s"$classFile (exit ${error.exitCode})" :: error.lines)

        StageAttempt.Failure(JavapErrorResponse(errors))

      case Nil =>
        val outputs =
          javapResults.collect:
            case (classFile, Right(classLines)) =>
              JavapClassResponse(classFile, classLines)

        StageAttempt.Success(JavapResponse(outputs))

  private def toCompilerResponse(compilerPhases: List[ScalaCompiler.Phase]): CompilerResponse =
    CompilerResponse(
      lines  = dumpCompilerPhases(compilerPhases),
      phases = compilerPhases.map: phase =>
        CompilerPhaseResponse(phase.hint, phase.lines)
    )

  private def dumpCompilerPhases(phases: List[ScalaCompiler.Phase]): String =
    phases
      .map: phase =>
        val header = s"[[${phase.hint}]]"
        if phase.lines.isEmpty then header
        else s"$header\n${phase.lines.mkString("\n")}"
      .mkString("\n\n")

  given Encoder[CompileAndDisassembleResponse] = deriveEncoder
