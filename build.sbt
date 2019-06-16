import sbtcrossproject.{crossProject, CrossType}

lazy val commonSettings = Seq(
  organization := "com.gubbns",
  homepage := Some(url(s"https://slakah.github.io/${name.value}/")),
  licenses += "MIT" -> url("http://opensource.org/licenses/MIT"),
  scmInfo := Some(ScmInfo(
    url(s"https://github.com/Slakah/${name.value}"),
    s"scm:git@github.com:Slakah/${name.value}.git"
  )),
  scalaVersion := "2.12.8",
  // https://scalacenter.github.io/scalafix/docs/users/installation.html
  addCompilerPlugin(scalafixSemanticdb),
  scalacOptions ++= scalacOpts :+ "-Yrangepos"
)

lazy val publishSettings = Seq(
  autoAPIMappings := true,
  publishMavenStyle := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  useGpg := false,
  pgpPassphrase ~= (_.orElse(sys.env.get("PGP_PASSPHRASE").map(_.toCharArray))),
  pgpPublicRing := file(s"./pubring.asc"),
  pgpSecretRing := file(s"./secring.asc"),
  apiURL := Some(url(s"https://slakah.github.io/fastparse-parsers/api/latest/${name.value}/")),
  pomExtra := {
    <developers>
      <developer>
        <id>slakah</id>
        <name>James Collier</name>
        <url>https://github.com/Slakah</url>
      </developer>
    </developers>
  }
)

lazy val betterMonadicForVersion = "0.3.0"
lazy val fastparseVersion = "2.1.3"
lazy val utestVersion = "0.6.9"
lazy val scalafixNoinferVersion = "0.1.0-M1"

ThisBuild / scalafixDependencies +=
  "com.eed3si9n.fix" %% "scalafix-noinfer" % scalafixNoinferVersion

ThisBuild / libraryDependencies +=
  compilerPlugin("com.olegpy" %% "better-monadic-for" % betterMonadicForVersion)

ThisBuild / releaseEarlyWith := SonatypePublisher
ThisBuild / releaseEarlyEnableLocalReleases := true
ThisBuild / organization := "com.gubbns"

publishArtifact := false

addCommandAlias("validate", Seq(
  "scalafixEnable",
  "scalafix --check",
  "test:scalafix --check",
  "test:compile",
  "+test",
  "tut").mkString(";", ";", "")
)

lazy val protobuf = crossProject(JSPlatform, JVMPlatform)
  .in(file("protobuf"))
  .jsSettings(
    // currently sbt-doctest doesn't work in JS builds
    // https://github.com/tkawachi/sbt-doctest/issues/52
    doctestGenTests := Seq.empty
  )
  .settings(
    commonSettings,
    publishSettings,
    name := "protobuf-fastparse",
    testFrameworks += new TestFramework("utest.runner.Framework"),
    doctestTestFramework := DoctestTestFramework.MicroTest,
    libraryDependencies ++= Seq(
      "com.lihaoyi" %%% "fastparse" % fastparseVersion
    ) ++ Seq(
      "com.lihaoyi" %%% "utest" % utestVersion
    ).map(_ % "test")
  )

lazy val protobufJS = protobuf.js
lazy val protobufJVM = protobuf.jvm

lazy val scalacOpts = Seq(
  "-deprecation",                      // Emit warning and location for usages of deprecated APIs.
  "-encoding", "utf-8",                // Specify character encoding used by source files.
  "-explaintypes",                     // Explain type errors in more detail.
  "-feature",                          // Emit warning and location for usages of features that should be imported explicitly.
  "-language:existentials",            // Existential types (besides wildcard types) can be written and inferred
  "-language:experimental.macros",     // Allow macro definition (besides implementation and application)
  "-language:higherKinds",             // Allow higher-kinded types
  "-language:implicitConversions",     // Allow definition of implicit functions called views
  "-unchecked",                        // Enable additional warnings where generated code depends on assumptions.
  "-Xcheckinit",                       // Wrap field accessors to throw an exception on uninitialized access.
  "-Xfatal-warnings",                  // Fail the compilation if there are any warnings.
  "-Xfuture",                          // Turn on future language features.
  "-Xlint:_",                          // enable all linting options
  "-Yno-adapted-args",                 // Do not adapt an argument list (either by inserting () or creating a tuple) to match the receiver.
  "-Ypartial-unification",             // Enable partial unification in type constructor inference
  "-Ywarn-dead-code",                  // Warn when dead code is identified.
  "-Ywarn-extra-implicit",             // Warn when more than one implicit parameter section is defined.
  "-Ywarn-inaccessible",               // Warn about inaccessible types in method signatures.
  "-Ywarn-infer-any",                  // Warn when a type argument is inferred to be `Any`.
  "-Ywarn-nullary-override",           // Warn when non-nullary `def f()' overrides nullary `def f'.
  "-Ywarn-nullary-unit",               // Warn when nullary methods return Unit.
  "-Ywarn-numeric-widen",              // Warn when numerics are widened.
  "-Ywarn-unused:-explicits,-patvars,_",        // Warn if declarations are unused.
  "-Ywarn-value-discard",              // Warn when non-Unit expression results are unused.
  "-Ywarn-macros:both"
)

lazy val noPublishSettings = Seq(
  skip in publish := true,
  PgpKeys.publishSigned := {},
  PgpKeys.publishLocalSigned := {},
  publishArtifact := false
)
