val akkaV = "2.5.6"
val akkaHttpVer = "10.0.9"
val circeVersion = "0.8.0"

val validate = Def.taskKey[Unit]("Validates entire project")

enablePlugins(DependencyGraphPlugin)

name := "jira-client"
organization := "io.morgaroth"
scalaVersion := "2.12.8"

resolvers ++= Seq(
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
  Resolver.bintrayRepo("morgaroth", "maven"),
)

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

fork in Test := true

javaOptions in Test += "-Duser.timezone=UTC"

libraryDependencies ++= Seq(
  //@formatter:off
  "com.iheart"              %% "ficus"          % "1.4.1",
  "com.github.nscala-time"  %% "nscala-time"    % "2.16.0",

  "com.typesafe.akka"       %% "akka-http"      % akkaHttpVer,
  "com.typesafe.akka"       %% "akka-slf4j"     % akkaV,
  "com.typesafe.akka"       %% "akka-stream"    % akkaV,

  "ch.megard"               %% "akka-http-cors"   % "0.1.11",
  "de.heikoseeberger"       %% "akka-http-circe"  % "1.18.0",

  "org.typelevel" %% "cats-core"      % "1.0.0",
  "io.circe"      %% "circe-core"     % circeVersion,
  "io.circe"      %% "circe-generic"  % circeVersion,
  "io.circe"      %% "circe-parser"   % circeVersion,

  "com.typesafe.scala-logging"    %% "scala-logging" % "3.7.2",

  "org.scalatest" %% "scalatest" % "3.0.4" % "test"
  //@formatter:on
)

publishArtifact in Test := false,

logBuffered := false

// Bintray
licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
bintrayVcsUrl := Some("https://gitlab.com/morgaroth/op-rabbit-rpc.git"),

// Release
releaseTagComment := s"Releasing ${(version in ThisBuild).value} [skip ci]",
releaseCommitMessage := s"Setting version to ${(version in ThisBuild).value} [skip ci]",

validate := Def.task {
  (Test / test).value
  //      tut.value
}.value
