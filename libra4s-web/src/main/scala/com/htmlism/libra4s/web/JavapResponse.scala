package com.htmlism.libra4s.web

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

enum JavapMemberRole(val json: String):
  case Plain  extends JavapMemberRole("plain")
  case Field  extends JavapMemberRole("field")
  case Method extends JavapMemberRole("method")

object JavapMemberRole:
  given Encoder[JavapMemberRole] =
    Encoder.encodeString.contramap(_.json)

final case class JavapMemberGroupResponse(
    role: JavapMemberRole,
    lines: List[String]
)

object JavapMemberGroupResponse:
  given Encoder[JavapMemberGroupResponse] =
    deriveEncoder

final case class JavapClassResponse(
    classFile: String,
    groups: List[JavapMemberGroupResponse]
)

object JavapClassResponse:
  given Encoder[JavapClassResponse] =
    deriveEncoder

final case class JavapResponse(outputs: List[JavapClassResponse])

object JavapResponse:
  given Encoder[JavapResponse] =
    deriveEncoder
