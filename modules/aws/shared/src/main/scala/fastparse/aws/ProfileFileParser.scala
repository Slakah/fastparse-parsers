package fastparse.aws

import fastparse._

object LiteralParser {
  import NoWhitespace._

  def letter[_: P]: P0 = P(CharIn("A-Z", "a-z"))
  def decimalDigit[_: P]: P0 = P(CharIn("0-9"))
  def ident[_: P]: P0 = P((letter | decimalDigit | CharIn("_\\-")).rep(1))
}

object DefinitionParser {
  import SingleLineWhitespace._
  import LiteralParser._

  def lineEnd[_: P]: P0 = P("\n" | "\r\n" | End)
  def ignoreLine[_: P]: P0 = commentLine | emptyLine
  def emptyLine[_: P]: P0 = P(CharIn(" \t\n\r") | End)
  def commentLine[_: P]: P0 = P(CharIn("#;") ~/ AnyChar.rep ~ lineEnd)

  def profileDefinitionLine[_: P]: P[String] = P(
    "[" ~/ ("default".! | ("profile".? ~/ ident.!.map(_.trim))) ~ "]" ~ lineEnd)
//    ((CharIn("#;") ~/ AnyChar.rep ~ lineEnd) | lineEnd))

  def propertyDefinitionLine[_: P]: P[(String, String)] = P(
    ident.! ~ "=" ~ (!"\n" ~ AnyChar).rep.! ~ lineEnd)

  def profileBody[_: P]: P[Seq[(String, String)]] = P(
    (ignoreLine.? ~ propertyDefinitionLine).rep(1)
  )
}

object ProfileFileParser {
  import DefinitionParser._
  import NoWhitespace._

  def profile[_: P]: P[Profile] = P(ignoreLine.? ~ (profileDefinitionLine ~/ profileBody.?))
    .map { case (name, properties) => Profile(name, properties.toList.flatten) }

  def profileFile[_: P]: P[ProfileFile] = P(Start ~/
    profile.rep.map(ProfileFile)
  ~ End)
}
