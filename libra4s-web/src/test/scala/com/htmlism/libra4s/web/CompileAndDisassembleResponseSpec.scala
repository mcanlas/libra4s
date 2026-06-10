package com.htmlism.libra4s.web

import io.circe.syntax.*
import weaver.FunSuite

import com.htmlism.libra4s.core.ProcessRunner.ProcessRunnerError
import com.htmlism.libra4s.core.ScalaCompiler

/**
  * Verifies the serialized compile API contract, including field names, removed fields, enum values, and attempt shape.
  *
  * Parser behavior and group contents are tested directly in the dedicated member-group parser specs.
  */
object CompileAndDisassembleResponseSpec extends FunSuite:
  test("compiler success encodes grouped phase schema without raw lines fields"):
    val res =
      CompileAndDisassembleResponse
        .fromCompileSuccessWithoutClass(
          List(
            ScalaCompiler.Phase(
              "parser",
              List(
                "def foo = 1",
                "  foo",
                "",
                "private[this] val bar = 2",
                "protected var baz = 3",
                "println(bar)"
              )
            )
          )
        )
        .asJson

    val compilerCursor =
      res
        .hcursor
        .downField("compiler")
        .downField("value")

    expect(compilerCursor.downField("lines").focus.isEmpty) &&
    expect(
      compilerCursor
        .downField("phases")
        .downN(0)
        .downField("hint")
        .as[String]
        .contains("parser")
    ) &&
    expect(
      compilerCursor
        .downField("phases")
        .downN(0)
        .downField("groups")
        .downN(0)
        .downField("role")
        .as[String]
        .contains("def")
    ) &&
    expect(
      compilerCursor
        .downField("phases")
        .downN(0)
        .downField("groups")
        .downN(0)
        .downField("lines")
        .as[List[String]]
        .contains(List("def foo = 1", "  foo", ""))
    ) &&
    expect(
      compilerCursor
        .downField("phases")
        .downN(0)
        .downField("groups")
        .downN(1)
        .downField("role")
        .as[String]
        .contains("val")
    ) &&
    expect(
      compilerCursor
        .downField("phases")
        .downN(0)
        .downField("groups")
        .downN(1)
        .downField("lines")
        .as[List[String]]
        .contains(List("private[this] val bar = 2"))
    ) &&
    expect(
      compilerCursor
        .downField("phases")
        .downN(0)
        .downField("groups")
        .downN(2)
        .downField("role")
        .as[String]
        .contains("var")
    ) &&
    expect(
      compilerCursor
        .downField("phases")
        .downN(0)
        .downField("groups")
        .downN(2)
        .downField("lines")
        .as[List[String]]
        .contains(List("protected var baz = 3"))
    ) &&
    expect(
      compilerCursor
        .downField("phases")
        .downN(0)
        .downField("groups")
        .downN(3)
        .downField("role")
        .as[String]
        .contains("plain")
    ) &&
    expect(
      compilerCursor
        .downField("phases")
        .downN(0)
        .downField("groups")
        .downN(3)
        .downField("lines")
        .as[List[String]]
        .contains(List("println(bar)"))
    ) &&
    expect(
      compilerCursor
        .downField("phases")
        .downN(0)
        .downField("groups")
        .downN(4)
        .focus
        .isEmpty
    )

  test("javap success encodes grouped class schema without raw lines fields"):
    val res =
      CompileAndDisassembleResponse
        .fromCompileSuccess(
          List(ScalaCompiler.Phase("parser", List("class Cat"))),
          List(
            "Cat.class" -> Right(
              List("Compiled from \"Cat.scala\"", "public final class Cat {")
            )
          )
        )
        .asJson

    val javapCursor =
      res
        .hcursor
        .downField("javap")
        .downField("value")

    expect(
      javapCursor
        .downField("outputs")
        .downN(0)
        .downField("lines")
        .focus
        .isEmpty
    ) &&
    expect(
      javapCursor
        .downField("outputs")
        .downN(0)
        .downField("classFile")
        .as[String]
        .contains("Cat.class")
    ) &&
    expect(
      javapCursor
        .downField("outputs")
        .downN(0)
        .downField("groups")
        .downN(0)
        .downField("role")
        .as[String]
        .contains("plain")
    ) &&
    expect(
      javapCursor
        .downField("outputs")
        .downN(0)
        .downField("groups")
        .downN(0)
        .downField("lines")
        .as[List[String]]
        .contains(List("Compiled from \"Cat.scala\"", "public final class Cat {"))
    )

  test("compiler grouping keeps adjacent same-role declarations in separate groups"):
    val res =
      CompileAndDisassembleResponse
        .fromCompileSuccessWithoutClass(
          List(
            ScalaCompiler.Phase(
              "parser",
              List(
                "def alpha = 1",
                "def beta = 2",
                "val gamma = 3"
              )
            )
          )
        )
        .asJson

    val groupsCursor =
      res
        .hcursor
        .downField("compiler")
        .downField("value")
        .downField("phases")
        .downN(0)
        .downField("groups")

    expect(
      groupsCursor
        .downN(0)
        .downField("role")
        .as[String]
        .contains("def")
    ) &&
    expect(
      groupsCursor
        .downN(0)
        .downField("lines")
        .as[List[String]]
        .contains(List("def alpha = 1"))
    ) &&
    expect(
      groupsCursor
        .downN(1)
        .downField("role")
        .as[String]
        .contains("def")
    ) &&
    expect(
      groupsCursor
        .downN(1)
        .downField("lines")
        .as[List[String]]
        .contains(List("def beta = 2"))
    ) &&
    expect(
      groupsCursor
        .downN(2)
        .downField("role")
        .as[String]
        .contains("val")
    ) &&
    expect(groupsCursor.downN(3).focus.isEmpty)

  test("compiler and javap role enums stay separate and string-encoded"):
    val compilerGroup =
      CompilerMemberGroupResponse(
        CompilerMemberRole.Def,
        List("def foo = 1")
      )

    val javapGroup =
      JavapMemberGroupResponse(
        JavapMemberRole.Method,
        List("public void foo();")
      )

    expect(compilerGroup.asJson.hcursor.downField("role").as[String].contains("def")) &&
    expect(javapGroup.asJson.hcursor.downField("role").as[String].contains("method"))

  test("compile failures still preserve process error payloads"):
    val res =
      CompileAndDisassembleResponse
        .fromCompileFailure(
          ProcessRunnerError(
            1,
            Nil,
            List("boom")
          )
        )
        .asJson

    expect(
      res
        .hcursor
        .downField("compiler")
        .downField("error")
        .downField("lines")
        .as[List[String]]
        .contains(List("boom"))
    )
