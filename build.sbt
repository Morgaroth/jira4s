val circeVersion = "0.12.3"
val circeExtVersion = "0.12.2"

val validate = Def.taskKey[Unit]("Validates entire project")

val projectScalaVersion = "2.13.3"
val crossScalaVersionsValues = Seq("2.12.12", projectScalaVersion)

val commonSettings = Seq(
  organization := "io.morgaroth",
  scalaVersion := projectScalaVersion,
  crossScalaVersions := crossScalaVersionsValues,

  resolvers ++= Seq(
    ("Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/").withAllowInsecureProtocol(true),
    Resolver.bintrayRepo("morgaroth", "maven").withAllowInsecureProtocol(true),
  ),

  scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8"),

  logBuffered := false,

  // Bintray
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  bintrayVcsUrl := Some("https://gitlab.com/morgaroth/op-rabbit-rpc.git"),
)

val core = project
  .settings(commonSettings: _*)
  .settings(
    name := "jira4s-core",
    libraryDependencies ++= Seq(
      "joda-time" % "joda-time" % "2.10.1",
      "org.typelevel" %% "cats-core" % "2.2.0",
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-generic-extras" % circeExtVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "com.typesafe" % "config" % "1.4.1",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
      "org.scalatest" %% "scalatest" % "3.2.2" % Test,
    )
  )

val sttp = project.in(file("sttp")).dependsOn(core)
  .settings(commonSettings: _*)
  .settings(
    name := "jira4s-sttp",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.client" %% "core" % "2.0.0-RC6",
      "org.scalatest" %% "scalatest" % "3.2.2" % Test,
      "ch.qos.logback" % "logback-classic" % "1.2.3" % Test,
    )
  )

val akka = project.in(file("akka-http")).dependsOn(core)
  .settings(commonSettings: _*)
  .settings(
    name := "jira4s-akka-http",
    libraryDependencies ++= Seq(
    )
  )

val jira4s = project.in(file(".")).aggregate(core, sttp, akka)
  .settings(
    name := "jira4s",
    publish := {},
    crossScalaVersions := crossScalaVersionsValues,

    validate := Def.task {
      (Test / test).value
      //      tut.value
    }.value,

    releaseTagComment := s"Releasing ${(version in ThisBuild).value} [skip ci]",
    releaseCommitMessage := s"Setting version to ${(version in ThisBuild).value} [skip ci]",
    releaseNextCommitMessage := s"Setting version to ${(version in ThisBuild).value} [skip ci]",
    releaseCrossBuild := true,
  )
