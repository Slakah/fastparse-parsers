package fastparse.aws

import fastparse._
import utest._

object ProfileFileParserTests extends TestSuite {

  private def runParseSuccessCheck(body: String)(profileFile: ProfileFile): Unit = {
    val result = parse(body, ProfileFileParser.profileFile(_))
    assert(result == Parsed.Success(profileFile, body.length))
  }

  private def runParseFailureCheck(body: String): Unit = {
    val result = parse(body, ProfileFileParser.profileFile(_))
    assert(!result.isSuccess)
  }

  override val tests = Tests {
    test("empty profiles with no properties") -
      runParseSuccessCheck("[profile foo]")(ProfileFile(List(Profile("foo", List.empty))))

    test("fail when profile definition doesn't end with brackets") -
      runParseFailureCheck("[profile foo")

    test("trim profile name") -
      runParseSuccessCheck("[profile \tfoo\t]")(ProfileFile(List(Profile("foo", List.empty))))

    test("tab separate profile name from profile prefix") -
      runParseSuccessCheck("[profile\tfoo]")(ProfileFile(List(Profile("foo", List.empty))))

    test("fail when properties be defined without a profile") -
      runParseFailureCheck("name = value")

    test("profiles can contain properties") - runParseSuccessCheck(
      """
        |[profile foo]
        |name = value
        |""".stripMargin)(ProfileFile(List(Profile("foo", List("name" -> "value")))))

    test("supports windows line endings") - runParseSuccessCheck(
      "[profile foo]\r\nname = value")(ProfileFile(List(Profile("foo", List("name" -> "value")))))

    test("property values can contain =") - runParseSuccessCheck(
      """
        |[profile foo]
        |name = val=ue
        |""".stripMargin)(ProfileFile(List(Profile("foo", List("name" -> "val=ue")))))

    test("property values can contain unicode") -  runParseSuccessCheck(
      s"""
        |[profile foo]
        |name = ${"\uD83D\uDE02"}
        |""".stripMargin)(ProfileFile(List(Profile("foo", List("name" -> "\uD83D\uDE02")))))

    test("profile with multiple properties") - runParseSuccessCheck(
      """
         |[profile foo]
         |name = value
         |name2 = value2
         |""".stripMargin)(ProfileFile(List(
            Profile("foo", List("name" -> "value", "name2" -> "value2"))
    )))

    test("trim property keys and value") - runParseSuccessCheck(
      "[profile foo]\nname \t= \tvalue \t"
    )(ProfileFile(List(
      Profile("foo", List("name" -> "value"))
    )))

    test("empty property values") - runParseSuccessCheck(
      "[profile foo]\nname="
    )(ProfileFile(List(
      Profile("foo", List("name" -> ""))
    )))

    test("fail when property key is empty") - runParseFailureCheck(
      "[profile foo]\n=value")

    test("fail when property definition uses : instead of =") - runParseFailureCheck(
      "[profile foo]\nkey : value")

    test("multiple profiles can omit properties") - runParseSuccessCheck(
      "[profile foo]\n[profile bar]"
    )(ProfileFile(List(
      Profile("foo", List.empty), Profile("bar", List.empty)
    )))
  }
}
