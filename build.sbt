import Syntax._
import com.jsuereth.sbtpgp.PgpKeys.publishSigned
import xerial.sbt.Sonatype.GitLabHosting

val circeVersion    = "0.13.0"
val circeExtVersion = "0.13.0"

val projectScalaVersion      = "2.13.6"
val crossScalaVersionsValues = Seq(projectScalaVersion, "2.12.13")

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
      releaseStepCommandAndRemaining("+publishSigned"),
      releaseStepCommand("sonatypeBundleRelease"),
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
  idePackagePrefix.invisible := Some("io.gitlab.mateuszjaje.jiraclient"),
  logBuffered := false,
)

val core = project
  .settings(commonSettings: _*)
  .settings(
    name := "jira4s-core",
    libraryDependencies ++= Seq(
      "org.typelevel"              %% "cats-core"                % "2.3.1",
      "io.circe"                   %% "circe-core"               % circeVersion,
      "io.circe"                   %% "circe-generic"            % circeVersion,
      "io.circe"                   %% "circe-generic-extras"     % circeExtVersion,
      "io.circe"                   %% "circe-parser"             % circeVersion,
      "com.typesafe"                % "config"                   % "1.4.1",
      "com.typesafe.scala-logging" %% "scala-logging"            % "3.9.2",
      "org.scalatest"              %% "scalatest-flatspec"       % "3.2.3" % Test,
      "org.scalatest"              %% "scalatest-shouldmatchers" % "3.2.3" % Test,
    ),
  )

val sttpjdk = project
  .in(file("sttpjdk"))
  .dependsOn(core)
  .settings(commonSettings: _*)
  .settings(
    name := "jira4s-sttp",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.client3" %% "core"                     % "3.0.0",
      "com.softwaremill.sttp.client3" %% "httpclient-backend"       % "3.0.0",
      "org.scalatest"                 %% "scalatest-flatspec"       % "3.2.3" % Test,
      "org.scalatest"                 %% "scalatest-shouldmatchers" % "3.2.3" % Test,
      "ch.qos.logback"                 % "logback-classic"          % "1.2.3" % Test,
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
    releaseTagComment := s"Releasing ${(ThisBuild / version).value} [skip ci]",
    releaseCommitMessage := s"Setting version to ${(ThisBuild / version).value} [skip ci]",
    releaseNextCommitMessage := s"Setting version to ${(ThisBuild / version).value} [skip ci]",
    releaseCrossBuild := true,
  )
