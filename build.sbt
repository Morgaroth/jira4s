val akkaV = "2.5.6"
val akkaHttpVer = "10.0.9"

val circeVersion = "0.11.1"

val validate = Def.taskKey[Unit]("Validates entire project")

val commonSettings = Seq(
  organization := "io.morgaroth",
  scalaVersion := "2.12.8",

  resolvers ++= Seq(
    "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
    Resolver.bintrayRepo("morgaroth", "maven"),
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
      "org.typelevel" %% "cats-core" % "1.0.0",
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "com.typesafe" % "config" % "1.3.3",

      "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
    )
  )

val sttp = project.in(file("sttp")).dependsOn(core)
  .settings(commonSettings: _*)
  .settings(
    name := "jira4s-sttp",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp" %% "core" % "1.5.11",
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

    validate := Def.task {
      (Test / test).value
      //      tut.value
    }.value,

    // Release
    releaseTagComment := s"Releasing ${(version in ThisBuild).value} [skip ci]",
    releaseCommitMessage := s"Setting version to ${(version in ThisBuild).value} [skip ci]",
  )