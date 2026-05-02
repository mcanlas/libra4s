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
          form(id := "decompiler-form", method := "POST", action := "/list")(
            div(cls := "actions")(
              button(id := "submit", `type` := "submit")("Run")
            ),
            textarea(
              id          := "source",
              name        := "source",
              placeholder := "Paste Scala source here"
            )
          ),
          div(id := "output")
        )
      )
    )
