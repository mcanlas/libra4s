package com.htmlism.libra4s.web

object JavapMemberGroupParser:
  def parse(lines: List[String]): List[JavapMemberGroupResponse] =
    lines match
      case Nil =>
        Nil

      case head :: tail =>
        val role = detectRole(head)

        role match
          case JavapMemberRole.Plain =>
            val (plainLines, rest) = tail.span(detectRole(_) == JavapMemberRole.Plain)

            JavapMemberGroupResponse(role, head :: plainLines) :: parse(rest)

          case _ =>
            val declarationIndent = indentation(head)
            val (bodyLines, rest) = tail.span(isBodyLine(_, declarationIndent))

            JavapMemberGroupResponse(role, head :: bodyLines) :: parse(rest)

  private def detectRole(line: String): JavapMemberRole =
    val memberLine = line.startsWith("  ") && !line.startsWith("    ")
    val signature  = line.trim

    if memberLine && signature.endsWith(";") && signature.contains("{}") then JavapMemberRole.Plain
    else if memberLine && signature.endsWith(";") && signature.contains("(") then JavapMemberRole.Method
    else if memberLine && signature.endsWith(";") then JavapMemberRole.Field
    else JavapMemberRole.Plain

  private def isBodyLine(line: String, declarationIndent: Int) =
    line.isBlank || indentation(line) > declarationIndent

  private def indentation(line: String) =
    line.segmentLength(_.isWhitespace)
