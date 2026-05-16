package com.htmlism.libra4s.web

import scala.io.Source
import scala.util.Using

import weaver.FunSuite

object DecompilerAssetsSpec extends FunSuite:
  private val decompilerJsResource =
    "decompiler.js"

  private val localStorageJsResource =
    "local-storage.js"

  private lazy val maybeDecompilerJavascript =
    readResource(decompilerJsResource)

  private lazy val maybeLocalStorageJavascript =
    readResource(localStorageJsResource)

  private def readResource(resource: String) =
    Option(getClass.getClassLoader.getResourceAsStream(resource))
      .map(stream => Using.resource(Source.fromInputStream(stream))(_.mkString))

  private def withDecompilerJs[A](f: String => A) =
    maybeDecompilerJavascript match
      case Some(javascript) =>
        f(javascript)

      case None =>
        failure(s"missing test resource: $decompilerJsResource")

  private def withLocalStorageJs[A](f: String => A) =
    maybeLocalStorageJavascript match
      case Some(javascript) =>
        f(javascript)

      case None =>
        failure(s"missing test resource: $localStorageJsResource")

  test("loads encapsulated local storage API and restores saved source on page load"):
    withDecompilerJs: js =>
      expect(js.contains("window.libra4sLocalStorage")) &&
        expect(js.contains("const localStorageApi =")) &&
        expect(js.contains("const savedSource = localStorageApi.readSavedSource();")) &&
        expect(js.contains("source.value = savedSource;"))

  test("persists source only after successful compiler attempts through local storage API"):
    withDecompilerJs: js =>
      expect(js.contains("const isSuccessfulCompilerAttempt = attempt => attempt?.state === \"success\";")) &&
        expect(js.contains("if (isSuccessfulCompilerAttempt(data.compiler)) {")) &&
        expect(js.contains("localStorageApi.saveSource(source.value);"))

  test("auto-submits on edit after debounce without submitting restored source on load"):
    withDecompilerJs: js =>
      expect(js.contains("const autoSubmitDelayMs = 2000;")) &&
        expect(js.contains("""source.addEventListener("input", () => {""")) &&
        expect(js.contains("pendingAutoSubmit = window.setTimeout(runCompile, autoSubmitDelayMs);")) &&
        expect(js.contains("if (savedSource !== null) {"))

  test("manual run still submits immediately and clears pending auto-submit"):
    withDecompilerJs: js =>
      expect(js.contains("const clearPendingAutoSubmit = () => {")) &&
        expect(js.contains("clearPendingAutoSubmit();")) &&
        expect(js.contains("await runCompile();"))

  test("renders disassembly output grouped by class file headings"):
    withDecompilerJs: js =>
      expect(js.contains("const formatJavapOutputsHtml = outputs => {")) &&
        expect(js.contains("""<details class="javap-class"""")) &&
        expect(js.contains("""<summary class="javap-class-summary">""")) &&
        expect(js.contains("const hasJavapOutputs = javap =>")) &&
        expect(
          js.contains("""outputDisassembly.classList.toggle("has-structured-output", hasJavapOutputs(data.javap));""")
        )

  test("local storage helper reads non-empty source and handles write failures"):
    withLocalStorageJs: js =>
      expect(js.contains("const sourceStorageKey = \"libra4s.source\";")) &&
        expect(js.contains("window.localStorage.getItem(key)")) &&
        expect(js.contains("value.length > 0 ? value : null")) &&
        expect(js.contains("window.localStorage.setItem(key, value)")) &&
        expect(js.contains("window.libra4sLocalStorage = {"))
