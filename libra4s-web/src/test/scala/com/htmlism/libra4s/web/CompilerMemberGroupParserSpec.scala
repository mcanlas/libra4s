package com.htmlism.libra4s.web

import scala.io.Source
import scala.util.Using

import weaver.FunSuite

object CompilerMemberGroupParserSpec extends FunSuite:
  private val dogSource =
    "case class Dog(n: String)"

  private val dogCompilerLines =
    readFixture("member-groups/dog-compiler.txt")

  test(s"groups compiler declarations emitted for `$dogSource`"):
    val groups =
      CompilerMemberGroupParser.parse(dogCompilerLines)

    val declarations =
      groups.collect:
        case CompilerMemberGroupResponse(role, lines) if role != CompilerMemberRole.Plain =>
          role -> lines

    expect(
      declarations == List(
        CompilerMemberRole.Val -> List("    val n: String"),
        CompilerMemberRole.Def -> List("    def copy(n: String): Dog = new Dog(n)"),
        CompilerMemberRole.Def -> List("    def copy$default$1: String @uncheckedVariance = Dog.this.n"),
        CompilerMemberRole.Def -> List("    def _1: String = this.n"),
        CompilerMemberRole.Val -> List("  final lazy module val Dog: Dog = new Dog()"),
        CompilerMemberRole.Def -> List("    def apply(n: String): Dog = new Dog(n)"),
        CompilerMemberRole.Def -> List("    def unapply(x$1: Dog): Dog = x$1"),
        CompilerMemberRole.Def -> List("    override def toString: String = \"Dog\"")
      )
    )

  test("recognizes access and compiler-generated declaration modifiers"):
    val groups =
      CompilerMemberGroupParser.parse(
        List(
          "private[this] val n: String",
          "protected var nickname: String",
          "case val cached: String",
          "inline def label: String"
        )
      )

    expect(
      groups.map(_.role) == List(
        CompilerMemberRole.Val,
        CompilerMemberRole.Var,
        CompilerMemberRole.Val,
        CompilerMemberRole.Def
      )
    )

  test("keeps three adjacent defs in three separate line groups"):
    val lines =
      List(
        "def first: Int = 1",
        "def second: Int = 2",
        "def third: Int = 3"
      )

    expect(
      CompilerMemberGroupParser.parse(lines) ==
        lines.map: line =>
          CompilerMemberGroupResponse(CompilerMemberRole.Def, List(line))
    )

  test("a def consumes its more-indented body without parsing nested declarations"):
    val lines =
      List(
        "def describe: String =",
        "  val prefix = \"dog\"",
        "  def suffix = \"!\"",
        "  prefix + suffix",
        "val name: String"
      )

    expect(
      CompilerMemberGroupParser.parse(lines) == List(
        CompilerMemberGroupResponse(CompilerMemberRole.Def, lines.take(4)),
        CompilerMemberGroupResponse(CompilerMemberRole.Val, List("val name: String"))
      )
    )

  test("preserves every compiler fixture line in source order"):
    val reconstructed =
      CompilerMemberGroupParser
        .parse(dogCompilerLines)
        .flatMap(_.lines)

    expect(reconstructed == dogCompilerLines)

  test("returns no compiler groups for no emitted lines"):
    expect(CompilerMemberGroupParser.parse(Nil).isEmpty)

  private def readFixture(path: String) =
    Using.resource(Source.fromResource(path))(_.getLines().toList)
