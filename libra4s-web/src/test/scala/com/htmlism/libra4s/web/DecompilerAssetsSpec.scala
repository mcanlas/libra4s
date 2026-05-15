package com.htmlism.libra4s.web

import scala.io.Source
import scala.util.Using

import weaver.FunSuite

object DecompilerAssetsSpec extends FunSuite:
  private val jsResource =
    "decompiler.js"

  private lazy val maybeJavascript =
    readJavascript

  private def readJavascript =
    Option(getClass.getClassLoader.getResourceAsStream(jsResource))
      .map(stream => Using.resource(Source.fromInputStream(stream))(_.mkString))

  private def withDecompilerJs[A](f: String => A) =
    maybeJavascript match
      case Some(javascript) =>
        f(javascript)

      case None =>
        failure(s"missing test resource: $jsResource")

  test("restores saved source on page load only when a non-empty value exists"):
    withDecompilerJs: js =>
      expect(js.contains("""const sourceStorageKey = "libra4s.source";""")) &&
        expect(js.contains("const savedSource = window.localStorage.getItem(sourceStorageKey);")) &&
        expect(js.contains("savedSource.length > 0")) &&
        expect(js.contains("source.value = savedSource;"))

  test("persists source only after successful compiler attempts"):
    withDecompilerJs: js =>
      expect(js.contains("""const isSuccessfulCompilerAttempt = attempt => attempt?.state === "success";""")) &&
        expect(js.contains("if (isSuccessfulCompilerAttempt(data.compiler)) {")) &&
        expect(js.contains("saveSource(source.value);"))
