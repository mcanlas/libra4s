package com.htmlism.libra4s.web

import scala.io.Source
import scala.util.Using

import weaver.FunSuite

object JavapMemberGroupParserSpec extends FunSuite:
  private val dogSource =
    "case class Dog(n: String)"

  private val dogJavapLines =
    readFixture("member-groups/dog-javap.txt")

  test(s"groups javap signatures emitted for `$dogSource`"):
    val groups =
      JavapMemberGroupParser.parse(dogJavapLines)

    val members =
      groups.collect:
        case JavapMemberGroupResponse(role, lines) if role != JavapMemberRole.Plain =>
          role -> lines

    expect(
      members == List(
        JavapMemberRole.Field  -> List("  private final java.lang.String n;"),
        JavapMemberRole.Method -> dogJavapLines.slice(5, 12),
        JavapMemberRole.Method -> dogJavapLines.slice(12, 18),
        JavapMemberRole.Method -> dogJavapLines.slice(18, 22)
      )
    )

  test("keeps adjacent javap signatures in separate groups"):
    val lines =
      List(
        "  public int first();",
        "  public int second();",
        "  public int third();"
      )

    expect(
      JavapMemberGroupParser.parse(lines) ==
        lines.map: line =>
          JavapMemberGroupResponse(JavapMemberRole.Method, List(line))
    )

  test("preserves every javap fixture line in source order"):
    val reconstructed =
      JavapMemberGroupParser
        .parse(dogJavapLines)
        .flatMap(_.lines)

    expect(reconstructed == dogJavapLines)

  test("keeps static initializers plain"):
    val staticInitializerGroup =
      JavapMemberGroupParser
        .parse(dogJavapLines)
        .find(_.lines.contains("  static {};"))

    expect(staticInitializerGroup.exists(_.role == JavapMemberRole.Plain))

  test("returns no javap groups for no emitted lines"):
    expect(JavapMemberGroupParser.parse(Nil).isEmpty)

  private def readFixture(path: String) =
    Using.resource(Source.fromResource(path))(_.getLines().toList)
