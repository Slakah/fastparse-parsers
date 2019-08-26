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
    test("fail to parse an invalid expression") - {
      val body =
        """
          |syntax = "proto3";
          |message Foo {
        """.stripMargin
      val result = parse(body, ProtoFileParser.proto(_))
      assertMatch(result) {
        case failure: Parsed.Failure if failure.trace().msg == "Expected \"}\":4:9, found \"\"" => ()
      }
    }
    test("fail to parse proto without syntax expression") - {
      val result = parse("", ProtoFileParser.proto(_))
      assert(!result.isSuccess)
    }
    test("parse reserved expression") - {
      val body =
        """
          |syntax = "proto3";
          |message Foo {
          |  reserved 1;
          |  reserved 4 to 7, 1000 to max;
          |}
        """.stripMargin
      val result = parse(body, ProtoFileParser.proto(_))
      val proto = ProtoFile(List(Message("Foo", List(
        ReservedRanges(List(
          Range(1, None)
        )),
        ReservedRanges(List(
          Range(4, Some(IntToRange(7))),
          Range(1000, Some(MaxToRange))
        ))
      ))))
      assert(result == Parsed.Success(proto, body.length))
    }
    test("parse service definition") - {
      val body =
        """
          |syntax = "proto3";
          |service SearchService {
          |  rpc Search (SearchRequest) returns (SearchResponse);
          |}
        """.stripMargin
      val result = parse(body, ProtoFileParser.proto(_))
      val proto = ProtoFile(List(
        Service("SearchService", List(Rpc("Search", "SearchRequest", "SearchResponse", List.empty)))
      ))
      assert(result == Parsed.Success(proto, body.length))
    }
    test("examples") - {
      test("parse addressbook.proto example") -
        runProtoEqualsCheck("addressbook.proto", ExampleProtos.addressbook)

      test("parse wrappers_test.proto example") -
        runProtoEqualsCheck("wrappers_test.proto", ExampleProtos.wrappersTest)

      test("parse proto3_example.proto example") -
        runProtoEqualsCheck("proto3_example.proto", ExampleProtos.proto3Example)

      test("parse proto3_message.proto example") -
        runProtoEqualsCheck("proto3_message.proto", ExampleProtos.proto3Message)
    }
  }
}
