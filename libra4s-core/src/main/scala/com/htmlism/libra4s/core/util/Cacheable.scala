package com.htmlism.libra4s.core.util

trait Cacheable[A]:
  def canonicalString(a: A): String

object Cacheable:
  def slug[A](a: A)(using cacheable: Cacheable[A]): String =
    Hashing
      .sha256Hex(cacheable.canonicalString(a))
