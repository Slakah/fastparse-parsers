package fastparse.protobuf

import fastparse._
import utest._

object ProtoFileParserTests extends TestSuite {

  private def readResource(path: String) =
    scala.io.Source.fromResource(path).getLines.mkString("\n")


  private def runProtoEqualsCheck(path: String, proto: ProtoFile): Unit = {
    val body = readResource(path)
    val result = parse(body, ProtoFileParser.proto(_))
    assert(result == Parsed.Success(proto, body.length))
  }

  override val tests = Tests {
    test("parse empty proto") - {
      val body = """syntax = "proto3"; """
      val result = parse(body, ProtoFileParser.proto(_))
      assert(result == Parsed.Success(ProtoFile(Seq.empty), body.length))
    }
    test("fail to parse proto2") - {
      val body = """syntax = "proto2"; """
      val result = parse(body, ProtoFileParser.proto(_))
      assert(!result.isSuccess)
    }
    test("fail to parse proto without syntax expression") - {
      val result = parse("", ProtoFileParser.proto(_))
      assert(!result.isSuccess)
    }
    test("parse addressbook.proto example") -
      runProtoEqualsCheck("addressbook.proto", ExampleProtos.addressbook)

    test("parse wrappers_test.proto example") -
      runProtoEqualsCheck("wrappers_test.proto", ExampleProtos.wrappersTest)

    test("parse proto3_message.proto example") -
      runProtoEqualsCheck("proto3_message.proto", ExampleProtos.proto3Message)
  }

}
