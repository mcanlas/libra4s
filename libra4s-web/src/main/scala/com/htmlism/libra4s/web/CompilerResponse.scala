package com.htmlism.libra4s.web

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

enum CompilerMemberRole(val json: String):
  case Plain extends CompilerMemberRole("plain")
  case Def   extends CompilerMemberRole("def")
  case Val   extends CompilerMemberRole("val")
  case Var   extends CompilerMemberRole("var")

object CompilerMemberRole:
  given Encoder[CompilerMemberRole] =
    Encoder.encodeString.contramap(_.json)

final case class CompilerMemberGroupResponse(
    role: CompilerMemberRole,
    lines: List[String]
)

object CompilerMemberGroupResponse:
  given Encoder[CompilerMemberGroupResponse] =
    deriveEncoder

final case class CompilerPhaseResponse(
    hint: String,
    groups: List[CompilerMemberGroupResponse]
)

object CompilerPhaseResponse:
  given Encoder[CompilerPhaseResponse] =
    deriveEncoder

final case class CompilerResponse(phases: List[CompilerPhaseResponse])

object CompilerResponse:
  given Encoder[CompilerResponse] =
    deriveEncoder
