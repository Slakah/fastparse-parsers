package fastparse.protobuf

import fastparse._
import utest._

object ProtoFileParserTests extends TestSuite {

  private def readResource(path: String) =
    scala.io.Source.fromResource(path).getLines.mkString("\n")

  override val tests = Tests {
    test("parse addressbook.proto example") - {
      val body = readResource("addressbook.proto")
      val result = parse(body, ProtoFileParser.proto(_))
      assert(result == Parsed.Success(expAddressbookProto, 1319))
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

}
