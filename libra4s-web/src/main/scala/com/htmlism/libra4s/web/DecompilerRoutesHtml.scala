package com.htmlism.libra4s.web

import scalatags.Text
import scalatags.Text.all.*

object DecompilerRoutesHtml:
  val index: Text.TypedTag[String] =
    html(
      head(
        meta(charset := "UTF-8"),
        tag("title")("libra4s"),
        link(rel   := "stylesheet", href := "/decompiler.css"),
        script(src := "/decompiler.js")
      ),
      body(
        div(cls := "page")(
          form(id := "decompiler-form", method := "POST", action := "/compile")(
            div(cls := "actions")(
              button(id := "submit", `type` := "submit")("Run")
            ),
            textarea(
              id          := "source",
              name        := "source",
              placeholder := "Paste Scala source here"
            )
          ),
          div(id := "outputs")(
            div(cls := "output-column")(
              h2(
                span(cls := "pane-title")("Compiler Output"),
                span(id := "compiler-stage-icon", cls := "stage-icon", attr("aria-live") := "polite")
              ),
              div(id := "output-compiler", cls := "output-body")
            ),
            div(cls := "output-column")(
              h2(
                span(cls := "pane-title")("Disassembly Output"),
                span(id := "disassembly-stage-icon", cls := "stage-icon", attr("aria-live") := "polite")
              ),
              div(id := "output-disassembly", cls := "output-body")
            )
          )
        )
      )
    )
