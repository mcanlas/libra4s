package com.htmlism.libra4s.web

object CompilerMemberGroupParser:
  def parse(lines: List[String]): List[CompilerMemberGroupResponse] =
    lines match
      case Nil =>
        Nil

      case head :: tail =>
        val role = detectRole(head)

        role match
          case CompilerMemberRole.Plain =>
            val (plainLines, rest) = tail.span(detectRole(_) == CompilerMemberRole.Plain)

            CompilerMemberGroupResponse(role, head :: plainLines) :: parse(rest)

          case _ =>
            val declarationIndent = indentation(head)
            val (bodyLines, rest) = tail.span(isBodyLine(_, declarationIndent))

            CompilerMemberGroupResponse(role, head :: bodyLines) :: parse(rest)

  private def detectRole(line: String): CompilerMemberRole =
    line.dropWhile(_.isWhitespace) match
      case l if isDeclaration(l, "def") =>
        CompilerMemberRole.Def

      case l if isDeclaration(l, "val") =>
        CompilerMemberRole.Val

      case l if isDeclaration(l, "var") =>
        CompilerMemberRole.Var

      case _ =>
        CompilerMemberRole.Plain

  private def isDeclaration(line: String, keyword: String) =
    line.matches(
      s"""(?:(?:private|protected)(?:\\[[^]]+\\])?\\s+|(?:override|final|lazy|inline|transparent|implicit|case|module)\\s+)*$keyword(?:\\s|\\().*"""
    )

  private def isBodyLine(line: String, declarationIndent: Int) =
    line.isBlank || indentation(line) > declarationIndent

  private def indentation(line: String) =
    line.segmentLength(_.isWhitespace)
