package com.htmlism.libra4s.web

import cats.data.EitherT
import cats.effect.IO
import org.http4s.Request
import org.http4s.Response
import org.http4s.UrlForm

given Conversion[EitherT[IO, Response[IO], Response[IO]], IO[Response[IO]]] with
  def apply(ei: EitherT[IO, Response[IO], Response[IO]]): IO[Response[IO]] =
    ei.merge

def getFormData(req: Request[IO]): EitherT[IO, Nothing, UrlForm] =
  EitherT.right:
    req
      .as[UrlForm]
