package com.htmlism.libra4s.web

import scala.io.Source
import scala.util.Using

import weaver.FunSuite

object DecompilerAssetsSpec extends FunSuite:
  private val decompilerJsResource =
    "decompiler.js"

  private val localStorageJsResource =
    "local-storage.js"

  private val decompilerCssResource =
    "decompiler.css"

  private lazy val maybeDecompilerJavascript =
    readResource(decompilerJsResource)

  private lazy val maybeLocalStorageJavascript =
    readResource(localStorageJsResource)

  private lazy val maybeDecompilerCss =
    readResource(decompilerCssResource)

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

  private def withDecompilerCss[A](f: String => A) =
    maybeDecompilerCss match
      case Some(css) =>
        f(css)

      case None =>
        failure(s"missing test resource: $decompilerCssResource")

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

  test("renders compiler phase lines from grouped response fields"):
    withDecompilerJs: js =>
      expect(js.contains("const groupedLines = groups =>")) &&
        expect(js.contains("const lines = groupedLines(phase?.groups).join(\"\\n\");")) &&
        expect(!js.contains("Array.isArray(phase?.lines)"))

  test("renders javap class lines from grouped response fields"):
    withDecompilerJs: js =>
      expect(js.contains("output?.groups,")) &&
        expect(js.contains("formatLineWithCommentHighlight")) &&
        expect(!js.contains("Array.isArray(output?.lines)"))

  test("grouped line extraction preserves group and line ordering"):
    withDecompilerJs: js =>
      expect(
        js.contains(
          "groups.flatMap(group => Array.isArray(group?.lines) ? group.lines : [])"
        )
      )

  test("renders each compiler and javap group as a separately styled element"):
    withDecompilerJs: js =>
      expect(js.contains("const formatMemberGroupsHtml =")) &&
        expect(js.contains("member-group ${kind}-member-group")) &&
        expect(js.contains("member-role-${role}")) &&
        expect(js.contains("phase?.groups,")) &&
        expect(js.contains("\"compiler\",")) &&
        expect(js.contains("output?.groups,")) &&
        expect(js.contains("\"javap\","))

  test("assigns colors by semantic role instead of group position"):
    withDecompilerJs: js =>
      expect(!js.contains("memberPaletteSize")) &&
        expect(!js.contains("PaletteIndex")) &&
        expect(!js.contains("member-palette-"))

  test("alternates full and faded variants within consecutive same-role groups"):
    withDecompilerJs: js =>
      expect(js.contains("let previousRole = null;")) &&
        expect(js.contains("let sameRoleIndex = 0;")) &&
        expect(js.contains("sameRoleIndex = role === previousRole ? sameRoleIndex + 1 : 0;")) &&
        expect(js.contains("""const variant = sameRoleIndex % 2 === 0 ? "full" : "faded";""")) &&
        expect(js.contains("member-variant-${variant}"))

  test("defines independent semantic color mappings for compiler and javap roles"):
    withDecompilerCss: css =>
      expect(css.contains(".compiler-member-group.member-role-def")) &&
        expect(css.contains("rgba(76, 175, 80, 0.17)")) &&
        expect(css.contains(".compiler-member-group.member-role-val")) &&
        expect(css.contains("rgba(244, 67, 54, 0.15)")) &&
        expect(css.contains(".compiler-member-group.member-role-var")) &&
        expect(css.contains("rgba(255, 193, 7, 0.19)")) &&
        expect(css.contains(".javap-member-group.member-role-field")) &&
        expect(css.contains(".javap-member-group.member-role-method")) &&
        expect(css.contains(".member-group.member-role-plain"))

  test("defines faded variants from the same semantic base colors"):
    withDecompilerCss: css =>
      expect(css.contains(".compiler-member-group.member-role-def.member-variant-faded")) &&
        expect(css.contains("rgba(76, 175, 80, 0.04)")) &&
        expect(css.contains(".compiler-member-group.member-role-val.member-variant-faded")) &&
        expect(css.contains("rgba(244, 67, 54, 0.04)")) &&
        expect(css.contains(".compiler-member-group.member-role-var.member-variant-faded")) &&
        expect(css.contains("rgba(255, 193, 7, 0.05)")) &&
        expect(css.contains(".javap-member-group.member-role-field.member-variant-faded")) &&
        expect(css.contains(".javap-member-group.member-role-method.member-variant-faded")) &&
        expect(css.contains("rgba(33, 150, 243, 0.04)"))

  test("renders plain groups without a background or colored edge"):
    withDecompilerCss: css =>
      expect(
        css.contains(
          """.member-group.member-role-plain {
  border-left-color: transparent;
  background: transparent;
}"""
        )
      )

  test("adds ghost marker to compiler phase heading when phase body is blank"):
    withDecompilerJs: js =>
      expect(js.contains("const isBlankBody = lines.trim().length === 0;")) &&
        expect(js.contains("""const summarySuffix = isBlankBody ? " 👻" : "";""")) &&
        expect(js.contains("""<summary class="compiler-phase-summary">${escapeHtml(hint)}${summarySuffix}</summary>"""))

  test("local storage helper reads non-empty source and handles write failures"):
    withLocalStorageJs: js =>
      expect(js.contains("const sourceStorageKey = \"libra4s.source\";")) &&
        expect(js.contains("window.localStorage.getItem(key)")) &&
        expect(js.contains("value.length > 0 ? value : null")) &&
        expect(js.contains("window.localStorage.setItem(key, value)")) &&
        expect(js.contains("window.libra4sLocalStorage = {"))
