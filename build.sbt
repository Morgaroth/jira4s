import Syntax._
import com.jsuereth.sbtpgp.PgpKeys.publishSigned
import xerial.sbt.Sonatype.GitLabHosting

val circeVersion = "0.14.2"
val circeExtVersion = "0.14.2"
val scalatest = "3.2.13"

val projectScalaVersion = "2.13.8"
val crossScalaVersionsValues = Seq(projectScalaVersion, "3.2.0")

val publishSettings = Seq(
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  sonatypeProjectHosting := Some(GitLabHosting("mateuszjaje", "jira4s", "mateuszjaje@gmail.com")),
  developers := List(Developer("mjd", "Mateusz Jaje", "mateuszjaje@gmail.com", new URL("https://gitlab.com/mateuszjajedev"))),
  scalaVersion := projectScalaVersion,
  crossScalaVersions := crossScalaVersionsValues,
  publishTo := sonatypePublishToBundle.value,
  publishMavenStyle := true,
  versionScheme := Some("semver-spec"),
  sonatypeCredentialHost := "s01.oss.sonatype.org",
  releaseProcess := {
    import sbtrelease.ReleaseStateTransformations._
    Seq[ReleaseStep](
      checkSnapshotDependencies,
      inquireVersions,
      runClean,
      runTest,
      setReleaseVersion,
      commitReleaseVersion,
      tagRelease,
      setNextVersion,
      commitNextVersion,
      pushChanges,
    )
  },
)

val commonSettings = publishSettings ++ Seq(
  organization := "io.gitlab.mateuszjaje",
  resolvers += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/",
  scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8"),
  scalacOptions ++= {
    if (scalaVersion.value.startsWith("3.")) Seq("-Xmax-inlines", "64") else Seq.empty
  },
  idePackagePrefix.invisible := Some("io.gitlab.mateuszjaje.jiraclient"),
  logBuffered := false,
)

val core = project
  .settings(commonSettings: _*)
  .settings(
    name := "jira4s-core",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-core" % "2.8.0",
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion,
      "com.typesafe" % "config" % "1.4.2",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
      "org.scalatest" %% "scalatest-flatspec" % scalatest % Test,
      "org.scalatest" %% "scalatest-shouldmatchers" % scalatest % Test,
    ),
  )

val sttpjdk = project
  .in(file("sttpjdk"))
  .dependsOn(core)
  .settings(commonSettings: _*)
  .settings(
    name := "jira4s-sttp",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.client3" %% "core" % "3.7.6",
      "com.softwaremill.sttp.client3" %% "httpclient-backend" % "3.5.2",
      "org.scalatest" %% "scalatest-flatspec" % scalatest % Test,
      "org.scalatest" %% "scalatest-shouldmatchers" % scalatest % Test,
      "ch.qos.logback" % "logback-classic" % "1.2.11" % Test,
    ),
  )

val jira4s = project
  .in(file("."))
  .aggregate(core, sttpjdk)
  .settings(publishSettings)
  .settings(
    organization := "io.gitlab.mateuszjaje",
    name := "jira4s",
    publish := {},
    publishSigned := {},
    crossScalaVersions := crossScalaVersionsValues,
    releaseTagComment := s"Releasing ${(ThisBuild / version).value}",
    releaseCommitMessage := s"Setting version to ${(ThisBuild / version).value}\n[release commit]",
    releaseNextCommitMessage := s"Setting version to ${(ThisBuild / version).value}\n[skip ci]",
    releaseCrossBuild := true,
  )
