package com.htmlism.libra4s.web

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

final case class CompilerPhaseResponse(hint: String, lines: List[String])

object CompilerPhaseResponse:
  given Encoder[CompilerPhaseResponse] = deriveEncoder

final case class CompilerResponse(
    lines: String,
    phases: List[CompilerPhaseResponse]
)

object CompilerResponse:
  given Encoder[CompilerResponse] = deriveEncoder
