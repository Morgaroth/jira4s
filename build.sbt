val circeVersion = "0.13.0"
val circeExtVersion = "0.13.0"

val projectScalaVersion = "2.13.4"
val crossScalaVersionsValues = Seq(projectScalaVersion, "2.12.12")

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
  bintrayVcsUrl := Some("https://gitlab.com/morgaroth/jira4s.git"),
)

val core = project
  .settings(commonSettings: _*)
  .settings(
    name := "jira4s-core",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.2.0",
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-generic-extras" % circeExtVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "com.typesafe" % "config" % "1.4.1",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
      "org.scalatest" %% "scalatest" % "3.2.3" % Test,
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

val jira4s = project.in(file(".")).aggregate(core, sttp)
  .settings(
    name := "jira4s",
    publish := {},
    crossScalaVersions := crossScalaVersionsValues,

    releaseTagComment := s"Releasing ${(version in ThisBuild).value} [skip ci]",
    releaseCommitMessage := s"Setting version to ${(version in ThisBuild).value} [skip ci]",
    releaseNextCommitMessage := s"Setting version to ${(version in ThisBuild).value} [skip ci]",
    releaseCrossBuild := true,
  )
