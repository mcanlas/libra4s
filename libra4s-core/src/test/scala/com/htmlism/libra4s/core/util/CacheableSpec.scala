package com.htmlism.libra4s.core.util

import weaver.FunSuite

object CacheableSpec extends FunSuite:

  test("slug hashes the canonical string"):
    final case class Example(source: String, flags: List[String])

    given Cacheable[Example] with
      def canonicalString(a: Example): String =
        List(
          "example:v1",
          a.source,
          a.flags.mkString("|")
        ).mkString("\n")

    val slug =
      Cacheable
        .slug(Example("val x = 1", List("-Vprint:all", "-color:never")))

    val expected =
      Hashing
        .sha256Hex("example:v1\nval x = 1\n-Vprint:all|-color:never")

    expect.eql(expected, slug)
