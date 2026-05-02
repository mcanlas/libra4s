package com.htmlism.libra4s.web

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

final case class JavapResponse(lines: List[String])

object JavapResponse:
  given Encoder[JavapResponse] = deriveEncoder
