package fastparse.graphql

// https://graphql.github.io/graphql-spec/June2018/#sec-Language.Document

final case class Document(
  defs: List[Def]
)

sealed trait Def

// https://graphql.github.io/graphql-spec/June2018/#sec-Language.Operations

final case class OperationDef(
  op: OperationType,
  name: Option[String],
  variableDefs: Option[VariableDefs],
  directives: Option[Directives],
  selectionSet: SelectionSet
) extends Def

sealed trait OperationType
object OperationType {
  case object Query extends OperationType
  case object Mutation extends OperationType
  case object Subscription extends OperationType
}

// https://graphql.github.io/graphql-spec/June2018/#sec-Selection-Sets

final case class SelectionSet(value: Seq[Selection]) extends AnyVal
final case class Selection(
  field: Field,
  fragmentSpread: fragmentSpread,
  inlineFragment: inlineFragment
)

// https://graphql.github.io/graphql-spec/June2018/#sec-Language.Fields

final case class Field(
  alias: String,
  name: String,
  arguments: Arguments,
  directives: Directives,
  selectionSet: SelectionSet
)

// https://graphql.github.io/graphql-spec/June2018/#sec-Language.Arguments

final case class Arguments(value: Seq[Argument]) extends AnyVal
final case class Argument(name: String, value: Value)

// https://graphql.github.io/graphql-spec/June2018/#sec-Field-Alias

final case class Alias(name: String) extends AnyVal

// https://graphql.github.io/graphql-spec/June2018/#sec-Language.Fragments

final case class FragmentSpread(
  name: String,
  directives: Directives
)
final case class FragmentDef(
  name: String,
  typeCond: String,
  directives: Directives,
  selectionSet: SelectionSet
) extends Def

final case class InlineFragment(
  typeCond: Option[String],
  directives: Directives,
  selectionSet: SelectionSet
)

sealed trait Value
final case class Variable(value: String)
final case class IntValue(value: Int)
final case class FloatValue(value: Double)
final case class StringValue(value: String)
final case class BooleanValue(value: Boolean)
final case object NullValue
final case class EnumValue(value: String)
final case class ListValue(value: Seq[Value])
final case class ObjectValue(value: Map[String, Value])

sealed trait Type
final case class NamedType(name: String) extends Type
final case class ListType(value: Seq[Type]) extends Type
final case class NonNullNamedType(name: String) extends Type
final case class NonNullListType(value: Seq[Type]) extends Type

// https://graphql.github.io/graphql-spec/June2018/#sec-Type-System.Directives
final case class Directives(value: Seq[Directive]) extends AnyVal
final case class Directive(name: String, arguments: Arguments)

// https://graphql.github.io/graphql-spec/June2018/#sec-Type-System

final case class TypeSystemDef extends Def
