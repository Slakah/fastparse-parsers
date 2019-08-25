package fastparse.protobuf

import fastparse._
import utest._

object ProtoFileParserTests extends TestSuite {

  private def readResource(path: String) =
    scala.io.Source.fromResource(path).getLines.mkString("\n")

  private def runParseSuccessChecks(path: String): Unit = {
    val body = readResource(path)
    val result = parse(body, ProtoFileParser.proto(_))
    assert(result.isSuccess)
  }

  override val tests = Tests {
    test("parse addressbook.proto example") - {
      val body = readResource("addressbook.proto")
      val result = parse(body, ProtoFileParser.proto(_))
      assert(result == Parsed.Success(expAddressbookProto, 1319))
    }
    test("parse wrappers_test.proto example") - {
      val body = readResource("wrappers_test.proto")
      val result = parse(body, ProtoFileParser.proto(_))
      assert(result == Parsed.Success(expwrappersTestProto, 2477))
    }
    test("successfully parse examples") - {
      test("proto3_message.proto") - runParseSuccessChecks("proto3_message.proto")
    }
  }

  private lazy val expAddressbookProto = ProtoFile(List(
    Package("tutorial"),
    Import(None, "google/protobuf/timestamp.proto"),
    OptionExpr("java_package", StringConstant("com.example.tutorial")),
    OptionExpr("java_outer_classname", StringConstant("AddressBookProtos")),
    OptionExpr("csharp_namespace", StringConstant("Google.Protobuf.Examples.AddressBook")),
    Message("Person", List(
      Field(repeated = false, "string", "name", 1, List.empty),
      Field(repeated = false, "int32", "id", 2, List.empty),
      Field(repeated = false, "string", "email", 3, List.empty),
      Enum("PhoneType", List(
        EnumField("MOBILE", 0, List.empty),
        EnumField("HOME", 1, List.empty),
        EnumField("WORK", 2, List.empty)
      )),
      Message("PhoneNumber", List(
        Field(repeated = false, "string", "number", 1, List.empty),
        Field(repeated = false, "PhoneType", "type", 2, List.empty)
      )),
      Field(repeated = true, "PhoneNumber", "phones", 4, List.empty),
      Field(repeated = false, "google.protobuf.Timestamp", "last_updated", 5, List.empty)
    )),
    Message("AddressBook", List(
      Field(repeated = true, "Person", "people", 1, List.empty)
    ))
  ))

  private lazy val expwrappersTestProto = ProtoFile(List(
    Package("wrappers_test"),
    Import(None, "google/protobuf/wrappers.proto"),
    OptionExpr("java_package", StringConstant("com.google.protobuf.wrapperstest")),
    OptionExpr("java_outer_classname", StringConstant("WrappersTestProto")),
    Message("TopLevelMessage", List(
      Field(repeated = false, "int32", "field1" ,1,List.empty),
      Field(repeated = false,".google.protobuf.DoubleValue", "field_double", 2, List.empty),
      Field(repeated = false, ".google.protobuf.FloatValue", "field_float", 3, List.empty),
      Field(repeated = false, ".google.protobuf.Int64Value", "field_int64", 4, List.empty),
      Field(repeated = false, ".google.protobuf.UInt64Value", "field_uint64",5 ,List.empty),
      Field(repeated = false, ".google.protobuf.Int32Value", "field_int32", 6, List.empty),
      Field(repeated = false, ".google.protobuf.UInt32Value", "field_uint32", 7, List.empty),
      Field(repeated = false, ".google.protobuf.BoolValue", "field_bool", 8, List.empty),
      Field(repeated = false, ".google.protobuf.StringValue", "field_string", 9, List.empty),
      Field(repeated = false, ".google.protobuf.BytesValue", "field_bytes", 10, List.empty)
    ))
  ))
}
