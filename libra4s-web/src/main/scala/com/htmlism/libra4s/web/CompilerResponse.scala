package com.htmlism.libra4s.web

import com.htmlism.libra4s.core.ScalaCompiler.Phase

final case class CompilerResponse(phases: List[Phase])
