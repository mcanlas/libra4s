package com.htmlism.libra4s.web

import io.circe.Decoder
import io.circe.generic.semiauto.deriveDecoder

final case class CompileRequest(code: String)

object CompileRequest:
  given Decoder[CompileRequest] = deriveDecoder
