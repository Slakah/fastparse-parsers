package fastparse.protobuf

import fastparse._

@SuppressWarnings(Array("DisableSyntax.throw"))
object LexicalElementsParser {
  import NoWhitespace._
  // https://developers.google.com/protocol-buffers/docs/reference/proto3-spec#letters_and_digits
  def letter[_: P]: P0 = P(CharIn("A-Z", "a-z"))
  def decimalDigit[_: P]: P0 = P(CharIn("0-9"))
  def octalDigit[_: P]: P0 = P(CharIn("0-7"))
  def hexDigit[_: P]: P0 = P(CharIn("0-9", "A-F", "a-f"))
  // https://developers.google.com/protocol-buffers/docs/reference/proto3-spec#identifiers
  def ident[_: P]: P0 = P(letter ~ (letter | decimalDigit | "_").rep)
  def fullIdent[_: P]: P[String] = P(ident.rep(min = 1, sep = ".").!)
  def messageName[_: P]: P[String] = P(ident.!)
  def enumName[_: P]: P[String] = P(ident.!)
  def fieldName[_: P]: P[String] = P(ident.!)
  def oneofName[_: P]: P[String] = P(ident.!)
  def mapName[_: P]: P[String] = P(ident.!)
  def serviceName[_: P]: P[String] = P(ident.!)
  def rpcName[_: P]: P[String] = P(ident.!)
  def messageType[_: P]: P0 = P(".".? ~ (ident ~ ".").rep ~ messageName)
  def enumType[_: P]: P0 = P(".".? ~ (ident ~ ".").rep ~ enumName)
  // https://developers.google.com/protocol-buffers/docs/reference/proto3-spec#integer_literals
  def intLit[_: P]: P[Int] = P(
    decimalLit.!.map(Integer.parseInt) |
    octalLit.!.map(Integer.parseInt(_, 8)) |
    hexLit.!.map(Integer.parseInt(_, 16)))
  def decimalLit[_: P]: P0 = P(CharIn("0-9") ~ decimalDigit.rep)
  def octalLit[_: P]: P0 = P("0" ~ octalDigit.rep)
  def hexLit[_: P]: P0 = P("0" ~ CharIn("xX") ~ hexDigit.rep(1))
  // https://developers.google.com/protocol-buffers/docs/reference/proto3-spec#floating_point_literals
  def floatLit[_: P]: P0 = P(
    (
      (decimals ~ "." ~ decimals.? ~ exponent.?) |
      (decimals ~ exponent) |
      ("." ~ decimals ~ exponent.?)
    ) | StringIn("inf", "nan")
  )
  def decimals[_: P]: P0 = P(decimalDigit.rep(1))
  def exponent[_: P]: P0 = P(decimalDigit.rep(1))
  // https://developers.google.com/protocol-buffers/docs/reference/proto3-spec#boolean
  def boolLit[_: P]: P[Boolean] = P(StringIn("true", "false").!).map {
    case "true" => true
    case "false" => false
  }
  // https://developers.google.com/protocol-buffers/docs/reference/proto3-spec#string_literals
  def strLit[_: P]: P[String] = P(
    ("'" ~/ (!"'" ~ charValue).rep.! ~ "'") |
    ("\"" ~/ (!"\"" ~ charValue).rep.! ~ "\""))
  def charValue[_: P] = P(
    hexEscape | octEscape | charEscape |
    (!StringIn("\\0", "\n", "\\") ~ AnyChar))
  def hexEscape[_: P] = P("\\" ~ CharIn("xX") ~ hexDigit ~ hexDigit)
  def octEscape[_: P] = P("\\" ~ octalDigit ~ octalDigit ~ octalDigit)
  def charEscape[_: P] = P("\\" ~ CharIn("""abfnrtv\'""""))
  def quote[_: P] = CharIn("'\"")
  // https://developers.google.com/protocol-buffers/docs/reference/proto3-spec#empty_statement
  def emptyStatement[_: P] = P(";")
  // https://developers.google.com/protocol-buffers/docs/reference/proto3-spec#constant
  def constant[_: P]: P[Constant] = P(
    fullIdent.!.map(StringConstant) |
    (StringIn("-", "+").? ~ intLit).!.map(s => IntConstant(s.toInt)) |
    (StringIn("-", "+").? ~ floatLit).!.map {
      case "-inf" => FloatConstant(Float.NegativeInfinity)
      case "inf" | "+inf" => FloatConstant(Float.PositiveInfinity)
      case "+nan" | "nan" | "-nan" => FloatConstant(Float.NaN)
      case s => FloatConstant(s.toFloat)
    } |
    strLit.map(StringConstant) | boolLit.map(BooleanConstant))
}

@SuppressWarnings(Array("DisableSyntax.throw"))
object SyntaxParser {
  import JavaWhitespace._
  import LexicalElementsParser._
  // https://developers.google.com/protocol-buffers/docs/reference/proto3-spec#syntax
  def syntax[_: P] = P("syntax" ~/ "=" ~ quote ~ "proto3" ~ quote ~ ";")
  // https://developers.google.com/protocol-buffers/docs/reference/proto3-spec#import_statement
  def import_[_: P]: P[Import] = P("import" ~/ StringIn("weak", "public").!.? ~ strLit ~ ";").map {
    case (Some("weak"), fileName) => Import(Some(Import.WeakModifier), fileName)
    case (Some("public"), fileName) => Import(Some(Import.PublicModifier), fileName)
    case (Some(other), _) => throw new IllegalStateException(s"Unexpected import modifier found $other")
    case (None, fileName) => Import(None, fileName)
  }
  // https://developers.google.com/protocol-buffers/docs/reference/proto3-spec#package
  def package_[_: P]: P[Package] = P("package" ~/ fullIdent ~ ";").map(Package)
  // https://developers.google.com/protocol-buffers/docs/reference/proto3-spec#option
  def option[_: P]: P[OptionExpr] = P("option" ~/ optionName.! ~ "=" ~ constant ~ ";")
  .map { case (name, constant) => OptionExpr(name, constant) }
  def optionName[_: P] = P((ident | ("(" ~/ fullIdent ~ ")")) ~ ("." ~ ident).rep)
  // https://developers.google.com/protocol-buffers/docs/reference/proto3-spec#fields
  def type_[_: P] = P(
    "double" | "float" | "int32" | "int64" | "uint32" | "uint64" |
    "sint32" | "sint64" | "fixed32" | "fixed64" | "sfixed32" | "sfixed64" |
    "bool" | "string" | "bytes" | messageType | enumType)
  def fieldNumber[_: P]: P[Int] = P(intLit)
  // https://developers.google.com/protocol-buffers/docs/reference/proto3-spec#normal_field
  def field[_: P]: P[Field] =
    P("repeated".!.? ~ type_.! ~ fieldName ~ "=" ~ fieldNumber ~ ("[" ~/ fieldOptions ~ "]").? ~ ";")
    .map { case (r, t, n, num, opts) => Field(r.isDefined, t, n, num, opts.toList.flatten) }
  def fieldOptions[_: P]: P[Seq[OptionExpr]] = P(fieldOption.rep(min = 1, sep = ","))
  def fieldOption[_: P]: P[OptionExpr] = P(optionName.! ~ "=" ~ constant)
    .map { case (n, c) => OptionExpr(n, c) }
  // https://developers.google.com/protocol-buffers/docs/reference/proto3-spec#oneof_and_oneof_field
  def oneof[_: P]: P[Oneof] = P("oneof" ~/ oneofName ~ "{" ~/ (oneofField.map(Some.apply) | emptyStatement.map(_ => None)).rep ~ "}")
    .map { case (name, fields) => Oneof(name, fields.flatten) }
  def oneofField[_: P]: P[OneofField] = P(type_.! ~ fieldName ~ "=" ~ fieldNumber ~ ("[" ~/ fieldOptions ~ "]").? ~ ";")
    .map { case (t, n, num, opts) => OneofField(t, n, num, opts.toList.flatten) }
  // https://developers.google.com/protocol-buffers/docs/reference/proto3-spec#map_field
  def mapField[_: P]: P[MapField] = P("map" ~ "<" ~ keyType ~ "," ~ type_.! ~ ">" ~/ mapName ~ "=" ~ fieldNumber ~ ("[" ~/ fieldOptions ~ "]").? ~ ";")
    .map { case (kt, t, n, num, opts) => MapField(kt, t, n, num, opts.toList.flatten) }
  def keyType[_: P]: P[KeyType] = P(StringIn(
    "int32", "int64", "uint32", "uint64", "sint32", "sint64",
    "fixed32", "fixed64", "sfixed32", "sfixed64", "bool", "string").!)
    .map(s => KeyType.fromString(s).getOrElse(throw new IllegalStateException(s"Unsupported key type found $s")))
  // https://developers.google.com/protocol-buffers/docs/reference/proto3-spec#reserved
  def reserved[_: P] = P("reserved" ~/ (ranges | fieldNames) ~ ";")
  def ranges[_: P]: P[ReservedRanges] = P(range.rep(min = 1, sep = ","))
    .map(ReservedRanges)
  def range[_: P]: P[Range] = P(intLit ~ ("to" ~/ (intLit | "max".!)).?)
    .map {
      case (from, None) => Range(from, None)
      case (from, Some(to: Int)) => Range(from, Some(IntToRange(to)))
      case (from, Some("max")) => Range(from, Some(MaxToRange))
      case (_, Some(other)) => throw new IllegalStateException(s"Unexpected to in range found $other")
    }
  def fieldNames[_: P]: P[ReservedFieldNames] = P(fieldName.rep(min = 1, sep = ","))
    .map(ReservedFieldNames)
}

object TopLevelDefinitionsParser {
  import JavaWhitespace._
  import LexicalElementsParser._
  import SyntaxParser._

  // https://developers.google.com/protocol-buffers/docs/reference/proto3-spec#enum_definition
  def enum[_: P]: P[Enum] = P("enum" ~/ enumName ~ enumBody)
    .map { case (name, body) => Enum(name, body) }
  def enumBody[_: P]: P[Seq[EnumExpr]] = P("{" ~/ (option.map(Some.apply) | enumField.map(Some.apply) | emptyStatement.map(_ => None)).rep ~ "}").map(_.flatten)
  def enumField[_: P]: P[EnumField] = P(ident.! ~ "=" ~ intLit ~ ("[" ~/ enumValueOption.rep(min = 1, sep = ",") ~ "]").? ~ ";").map {
    case (name, number, Some(options)) => EnumField(name, number, options)
    case (name, number, None) => EnumField(name, number, List.empty)
  }
  def enumValueOption[_: P]: P[OptionExpr] = P(optionName.! ~ "=" ~ constant)
    .map { case (name, constant) => OptionExpr(name, constant) }
  // https://developers.google.com/protocol-buffers/docs/reference/proto3-spec#message_definition
  def message[_: P]: P[Message] = P("message" ~/ messageName ~ messageBody)
    .map { case (name, body) => Message(name, body) }
  def messageBody[_: P]: P[Seq[MessageExpr]] = P("{" ~/ (field.map(Some.apply) | enum.map(Some.apply) | message.map(Some.apply) | option.map(Some.apply) | oneof.map(Some.apply) | mapField.map(Some.apply) |
    reserved.map(Some.apply) | emptyStatement.map(_ => None)).rep.map(_.flatten) ~ "}")
  // https://developers.google.com/protocol-buffers/docs/reference/proto3-spec#service_definition
  def service[_: P] = P("service" ~/ serviceName ~ "{" ~/ (option.map(Some.apply) | rpc.map(Some.apply) | emptyStatement.map(_ => None)).rep.map(_.flatten) ~ "}")
    .map { case (name, body) => Service(name, body) }
  def rpc[_: P]: P[Rpc] = P("rpc" ~ rpcName ~ "(" ~/ "stream".? ~ messageType.! ~ ")" ~ "returns" ~ "(" ~/ "stream".? ~
    messageType.! ~ ")" ~ (( "{" ~/ (option.map(Some.apply) | emptyStatement.map(_ => None)).rep.map(_.flatten) ~ "}" ) | P(";").map(_ => List.empty)))
    .map { case (name, reqType, respType, options) => Rpc(name, reqType, respType, options) }
}

object ProtoFileParser {
  import JavaWhitespace._
  import LexicalElementsParser.emptyStatement
  import SyntaxParser._
  import TopLevelDefinitionsParser._

  def proto[_: P]: P[ProtoFile] = P(Start ~ syntax ~ (
      import_.map(List(_)) | package_.map(List(_)) | option.map(List(_)) |
      topLevelDef.map(List(_)) | emptyStatement.map(_ => List.empty)
    ).rep ~ End).map(l => ProtoFile(l.flatten))
  def topLevelDef[_: P]: P[Expr] = P(message | enum | service)
}
