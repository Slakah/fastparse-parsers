package fastparse.protobuf

sealed trait Constant
final case class StringConstant(value: String) extends Constant
final case class IntConstant(value: Int) extends Constant
final case class FloatConstant(value: Float) extends Constant
final case class BooleanConstant(value: Boolean) extends Constant

sealed trait KeyType

object KeyType {
  def fromString(s: String): Option[KeyType] = s match {
    case "int32" => Some(`int32`)
    case "int64" => Some(`int64`)
    case "uint32" => Some(`uint32`)
    case "uint64" => Some(`uint64`)
    case "sint32" => Some(`sint32`)
    case "sint64" => Some(`sint64`)
    case "fixed32" => Some(`fixed32`)
    case "fixed64" => Some(`fixed64`)
    case "sfixed32" => Some(`sfixed32`)
    case "sfixed64" => Some(`sfixed64`)
    case "bool" => Some(`bool`)
    case "string" => Some(`string`)
    case _ => None
  }
  
  case object `int32` extends KeyType
  case object `int64` extends KeyType
  case object `uint32` extends KeyType
  case object `uint64` extends KeyType
  case object `sint32` extends KeyType
  case object `sint64` extends KeyType
  case object `fixed32` extends KeyType
  case object `fixed64` extends KeyType
  case object `sfixed32` extends KeyType
  case object `sfixed64` extends KeyType
  case object `bool` extends KeyType
  case object `string` extends KeyType
}

sealed trait ToRange
final case class IntToRange(value: Int) extends ToRange
case object MaxToRange extends ToRange
final case class Range(from: Int, to: Option[ToRange])

final case class ProtoFile(
  exprs: Seq[Expr]
)

sealed trait Expr

sealed trait EnumExpr
sealed trait MessageExpr
sealed trait ServiceExpr

final case class Import(modifier: Option[Import.Modifier], fileName: String) extends Expr
object Import {
  sealed trait Modifier
  case object WeakModifier extends Modifier
  case object PublicModifier extends Modifier
}
final case class Package(`package`: String) extends Expr
final case class OptionExpr(name: String, value: Constant) extends Expr with EnumExpr with MessageExpr with ServiceExpr
final case class Enum(name: String, body: Seq[EnumExpr]) extends Expr with MessageExpr
final case class EnumField(name: String, number: Int, options: Seq[OptionExpr]) extends EnumExpr
final case class Message(name: String, body: Seq[MessageExpr]) extends Expr with MessageExpr
final case class Field(repeated: Boolean, `type`: String, name: String, number: Int, options: Seq[OptionExpr]) extends Expr with MessageExpr
final case class OneofField(`type`: String, name: String, number: Int, options: Seq[OptionExpr])
final case class Oneof(name: String, fields: Seq[OneofField]) extends MessageExpr
final case class MapField(keyType: KeyType, `type`: String, name: String, number: Int, options: Seq[OptionExpr]) extends MessageExpr
final case class ReservedRanges(ranges: Seq[Range]) extends MessageExpr
final case class ReservedFieldNames(names: Seq[String]) extends MessageExpr
final case class Service(name: String, body: Seq[ServiceExpr]) extends Expr
final case class Rpc(name: String, requestType: String, responseType: String, options: Seq[OptionExpr]) extends ServiceExpr
