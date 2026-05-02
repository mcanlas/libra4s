package com.htmlism.libra4s.web

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

import com.htmlism.libra4s.core.ScalaCompiler

final case class CompileAndDisassembleResponse(
    compiler: CompilerResponse,
    javap: JavapResponse
)

object CompileAndDisassembleResponse:
  def from(
      compilerPhases: List[ScalaCompiler.Phase],
      javapLines: List[String]
  ): CompileAndDisassembleResponse =
    CompileAndDisassembleResponse(
      compiler = CompilerResponse(
        lines  = dumpCompilerPhases(compilerPhases),
        phases = compilerPhases.map: phase =>
          CompilerPhaseResponse(phase.hint, phase.lines)
      ),
      javap = JavapResponse(javapLines)
    )

  private def dumpCompilerPhases(phases: List[ScalaCompiler.Phase]): String =
    phases
      .map: phase =>
        val header = s"[[${phase.hint}]]"
        if phase.lines.isEmpty then header
        else s"$header\n${phase.lines.mkString("\n")}"
      .mkString("\n\n")

  given Encoder[CompileAndDisassembleResponse] = deriveEncoder
