package com.htmlism.libra4s.core.util

import weaver.FunSuite

object HashingSpec extends FunSuite:

  test("sha256Hex returns the expected lowercase hex digest"):
    val digest =
      Hashing
        .sha256Hex("hello world")

    val expected =
      "b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9"

    expect.eql(expected, digest)
