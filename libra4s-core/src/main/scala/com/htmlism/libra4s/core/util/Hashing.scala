package com.htmlism.libra4s.core.util

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

object Hashing:
  private val Hex =
    "0123456789abcdef".toCharArray

  def sha256Hex(s: String): String =
    MessageDigest
      .getInstance("SHA-256")
      .digest(s.getBytes(StandardCharsets.UTF_8))
      .flatMap: b =>
        val i = b & 0xff
        Array(Hex(i >>> 4), Hex(i & 0x0f))
      .mkString
