package com.htmlism.libra4s.core.util

import io.circe.Decoder
import io.circe.Encoder
import io.circe.generic.semiauto.deriveDecoder
import io.circe.generic.semiauto.deriveEncoder
import weaver.SimpleIOSuite

object JsonFileCacheSpec extends SimpleIOSuite:

  final case class CachedInput(source: String, flags: List[String])

  object CachedInput:
    given Decoder[CachedInput] = deriveDecoder
    given Encoder[CachedInput] =
      deriveEncoder

    given Cacheable[CachedInput] with
      def canonicalString(a: CachedInput): String =
        List(
          "cached-input:v1",
          a.source,
          a.flags.mkString("|")
        ).mkString("\n")

  test("temporary cache writes and reads JSON payloads by cacheable input"):
    val input =
      CachedInput("val x = 1", List("-Vprint:all", "-color:never"))

    JsonFileCache
      .temporary
      .use: cache =>
        for
          before <- cache
            .get(input)

          written <- cache
            .write(input)

          after <- cache
            .get(input)
        yield expect(before.isEmpty) &&
          expect(written == input) &&
          expect(after.contains(input))
