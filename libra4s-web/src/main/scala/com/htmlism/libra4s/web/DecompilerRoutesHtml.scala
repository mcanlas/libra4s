package com.htmlism.libra4s.web

import scalatags.Text
import scalatags.Text.all.*

object DecompilerRoutesHtml:
  val index: Text.TypedTag[String] =
    html(
      head(
        meta(charset := "UTF-8"),
        tag("title")("libra4s")
      ),
      body(
        h1("libra4s"),
        form(method := "POST", action := "/list")(
          button(`type` := "submit")("List /tmp")
        )
      )
    )
