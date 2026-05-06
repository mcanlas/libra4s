package com.htmlism.libra4s.web

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

final case class JavapClassResponse(classFile: String, lines: List[String])

object JavapClassResponse:
  given Encoder[JavapClassResponse] = deriveEncoder

final case class JavapResponse(outputs: List[JavapClassResponse])

object JavapResponse:
  given Encoder[JavapResponse] = deriveEncoder
