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

enum StageAttempt[+A]:
  case Success(value: A)
  case Failure(error: ProcessErrorResponse)

object StageAttempt:
  given [A: Encoder]: Encoder.AsObject[StageAttempt[A]] =
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

final case class CompileAndDisassembleResponse(
    compiler: StageAttempt[CompilerResponse],
    javap: StageAttempt[JavapResponse]
)

object CompileAndDisassembleResponse:
  def fromCompileFailure(error: ProcessRunnerError): CompileAndDisassembleResponse =
    CompileAndDisassembleResponse(
      compiler = StageAttempt.Failure(toProcessErrorResponse(error)),
      javap    = StageAttempt.Failure(ProcessErrorResponse(error.exitCode, List("Compilation failed")))
    )

  def fromCompileSuccess(
      compilerPhases: List[ScalaCompiler.Phase],
      javapResult: Either[ProcessRunnerError, List[String]]
  ): CompileAndDisassembleResponse =
    CompileAndDisassembleResponse(
      compiler = StageAttempt.Success(toCompilerResponse(compilerPhases)),
      javap    = javapResult match
        case Left(error)  => StageAttempt.Failure(toProcessErrorResponse(error))
        case Right(lines) => StageAttempt.Success(JavapResponse(lines))
    )

  def fromCompileSuccessWithoutClass(compilerPhases: List[ScalaCompiler.Phase]): CompileAndDisassembleResponse =
    CompileAndDisassembleResponse(
      compiler = StageAttempt.Success(toCompilerResponse(compilerPhases)),
      javap    = StageAttempt.Failure(ProcessErrorResponse(0, List("No class files generated")))
    )

  private def toProcessErrorResponse(error: ProcessRunnerError): ProcessErrorResponse =
    val lines = if error.stderr.nonEmpty then error.stderr else error.stdout
    ProcessErrorResponse(error.exitCode, lines)

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
