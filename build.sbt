name := """catalyst-bias-correct"""

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.12.4"

dockerBaseImage := "openjdk:8-jre-alpine"
import com.typesafe.sbt.packager.docker._
dockerBuildOptions := Seq("--force-rm", "-t", s"${name.value}:${version.value}", "--network", "host")
dockerCommands ++= Seq(
  Cmd("USER", "root"),
  ExecCmd("RUN", "echo", "-e", "http://nl.alpinelinux.org/alpine/v3.8/main\\nhttp://nl.alpinelinux.org/alpine/v3.8/community", ">", "/etc/apk/repositories"),
  ExecCmd("RUN", "apk", "add", "--no-cache", "bash")
)

crossScalaVersions := Seq("2.11.12", "2.12.4")

libraryDependencies += guice

libraryDependencies ++= Seq(
  ws
)

// Test Database
libraryDependencies += "com.h2database" % "h2" % "1.4.196"

// Testing libraries for dealing with CompletionStage...
libraryDependencies += "org.assertj" % "assertj-core" % "3.6.2" % Test
libraryDependencies += "org.awaitility" % "awaitility" % "2.0.0" % Test

// https://mvnrepository.com/artifact/net.sf.jopt-simple/jopt-simple
libraryDependencies += "net.sf.jopt-simple" % "jopt-simple" % "5.0.4"

// https://mvnrepository.com/artifact/redis.clients/jedis
libraryDependencies += "redis.clients" % "jedis" % "3.0.1"

// https://mvnrepository.com/artifact/com.opencsv/opencsv
libraryDependencies += "com.opencsv" % "opencsv" % "4.4"

// https://mvnrepository.com/artifact/commons-codec/commons-codec
libraryDependencies += "commons-codec" % "commons-codec" % "1.12"

// https://mvnrepository.com/artifact/org.apache.opennlp/opennlp-tools
libraryDependencies += "org.apache.opennlp" % "opennlp-tools" % "1.9.1"

// Location of test files to run with sbt test
javaSource in Test := baseDirectory.value / "test"

// Make verbose tests
testOptions in Test := Seq(Tests.Argument(TestFrameworks.JUnit, "-a", "-v"))

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

// No documentation in output of sbt dist
sources in (Compile, doc) := Seq.empty
publishArtifact in (Compile, packageDoc) := false

// Build the package with just eskalera-bias-correction-java-latest
packageName in Universal := s"${name.value}-latest"

PlayKeys.devSettings := Seq("play.server.http.port" -> "4542")
