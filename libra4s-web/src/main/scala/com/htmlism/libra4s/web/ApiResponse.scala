package com.htmlism.libra4s.web

import io.circe.Encoder
import io.circe.JsonObject
import io.circe.syntax.*

enum ApiResponse[+E, +A](val ok: Boolean):
  case Success(data: A)  extends ApiResponse[Nothing, A](true)
  case Failure(error: E) extends ApiResponse[E, Nothing](false)

object ApiResponse:
  given [E: Encoder, A: Encoder]: Encoder.AsObject[ApiResponse[E, A]] =
    Encoder
      .AsObject
      .instance:
        case Success(data) =>
          JsonObject(
            "ok"   -> true.asJson,
            "data" -> data.asJson
          )

        case Failure(error) =>
          JsonObject(
            "ok"    -> false.asJson,
            "error" -> error.asJson
          )
