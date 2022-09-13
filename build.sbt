import Syntax._
import com.jsuereth.sbtpgp.PgpKeys.publishSigned
import xerial.sbt.Sonatype.GitLabHosting

val circeVersion    = "0.14.2"
val circeExtVersion = "0.14.2"
val scalatest       = "3.2.13"

val projectScalaVersion      = "2.13.8"
val crossScalaVersionsValues = Seq(projectScalaVersion, "3.1.2")

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
  scalacOptions := Seq(
    "-unchecked",
    "-deprecation",
    "-encoding",
    "utf8",
    "-feature",
    "-language:higherKinds",
    "-language:postfixOps",
    "-language:implicitConversions",
  ),
  scalacOptions ++= {
    if (scalaVersion.value.startsWith("2.13")) {
      Seq(
        "-Ymacro-annotations",
        "-Ywarn-unused:imports",
        "-Xsource:3",
        "-P:kind-projector:underscore-placeholders",
      )
    } else if (scalaVersion.value.startsWith("3.")) {
      Seq(
        "-Ykind-projector",
        "-Xmax-inlines",
        "110",
      )
    } else Seq.empty
  },
  libraryDependencies ++= {
    if (scalaVersion.value.startsWith("2.13"))
      Seq(compilerPlugin("org.typelevel" % "kind-projector" % "0.13.2" cross CrossVersion.full))
    else Seq.empty
  },
  idePackagePrefix.invisible := Some("io.gitlab.mateuszjaje.jiraclient"),
  logBuffered := false,
)

val testDeps = Seq(
  "org.scalatest" %% "scalatest-flatspec"       % "3.2.13" % Test,
  "org.scalatest" %% "scalatest-shouldmatchers" % "3.2.13" % Test,
  "ch.qos.logback" % "logback-classic"          % "1.2.11" % Test,
)

val core = project
  .settings(commonSettings: _*)
  .settings(
    name := "jira4s-core",
    libraryDependencies ++= Seq(
      "org.typelevel"              %% "cats-core"     % "2.8.0",
      "io.circe"                   %% "circe-core"    % circeVersion,
      "io.circe"                   %% "circe-generic" % circeVersion,
      "io.circe"                   %% "circe-parser"  % circeVersion,
      "com.typesafe"                % "config"        % "1.4.2",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
    ) ++ testDeps,
  )

val sttpjdk = project
  .in(file("sttpjdk"))
  .dependsOn(core)
  .settings(commonSettings: _*)
  .settings(
    name := "jira4s-sttp",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.client3" %% "core"               % "3.7.6",
      "com.softwaremill.sttp.client3" %% "httpclient-backend" % "3.5.2",
    ) ++ testDeps,
  )

val sttpzio1 = project
  .in(file("sttp-zio1"))
  .dependsOn(core)
  .settings(commonSettings: _*)
  .settings(
    name := "jira4s-sttp",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.client3" %% "core" % "3.7.6",
      "com.softwaremill.sttp.client3" %% "zio1" % "3.7.6", // for ZIO 1.x
    ) ++ testDeps,
  )

val sttpzio2 = project
  .in(file("sttp-zio"))
  .dependsOn(core)
  .settings(commonSettings: _*)
  .settings(
    name := "jira4s-sttp",
    libraryDependencies ++= Seq(
      "com.softwaremill.sttp.client3" %% "core" % "3.7.6",
      "com.softwaremill.sttp.client3" %% "zio"  % "3.7.6",
    ) ++ testDeps,
  )

val jira4s = project
  .in(file("."))
  .aggregate(core, sttpjdk, sttpzio2, sttpzio1)
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
