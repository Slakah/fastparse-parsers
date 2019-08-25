package fastparse.protobuf

import fastparse._

object Main {

  val s = """
// See README.txt for information and build instructions.
//
// Note: START and END tags are used in comments to define sections used in
// tutorials.  They are not part of the syntax for Protocol Buffers.
//
// To get an in-depth walkthrough of this file and the related examples, see:
// https://developers.google.com/protocol-buffers/docs/tutorials

// [START declaration]
syntax = "proto3";
package tutorial;

import "google/protobuf/timestamp.proto";
// [END declaration]

// [START java_declaration]
option java_package = "com.example.tutorial";
option java_outer_classname = "AddressBookProtos";
// [END java_declaration]

// [START csharp_declaration]
option csharp_namespace = "Google.Protobuf.Examples.AddressBook";
// [END csharp_declaration]

// [START messages]
message Person {
  string name = 1;
  int32 id = 2;  // Unique ID number for this person.
  string email = 3;
  reserved 2, 15, 9 to 11;
  reserved foo, bar;

  enum PhoneType {
    MOBILE = 0;
    HOME = 1;
    WORK = 2;
  }

  map<string, string> foo = 100 [fo=123];
  message PhoneNumber {
    string number = 1;
    PhoneType type = 2;
  }

  repeated PhoneNumber phones = 4;

  google.protobuf.Timestamp last_updated = 5;
}

// Our address book file is just one of these.
message AddressBook {
  repeated Person people = 1;
  oneof foo {
    string name = 4;
    SubMessage sub_message = 9;
  }
}
// [END messages]

"""

  def show(s: String): String =
    s.foldLeft(0 -> "") {
      case ((ident, acc), '(') =>
        ((ident + 2), s"$acc(\n${" " * (ident + 2)}")
      case ((ident, acc), ')') =>
        ((ident - 2), s"$acc\n${" " * (ident - 2)})")
      case ((ident, acc), c) =>
        ident -> (acc + c)
    }._2

  def main(args: Array[String]): Unit = {
    println(show(parse(s, ProtoFileParser.proto(_)).toString))
  }
}
