val circeVersion    = "0.13.0"
val circeExtVersion = "0.13.0"

val projectScalaVersion      = "2.13.6"
val crossScalaVersionsValues = Seq(projectScalaVersion, "2.12.13")

val publishSettings = Seq(
  scalaVersion := projectScalaVersion,
  crossScalaVersions := crossScalaVersionsValues,
  credentials += Credentials(file(sys.env.getOrElse("JFROG_CREDENTIALS_FILE", ".credentials"))),
  publishTo := Some {
    if (!isSnapshot.value) "Artifactory releases" at "https://jajemateuszdev.jfrog.io/artifactory/maven/"
    else "Artifactory snapshots" at s"https://jajemateuszdev.jfrog.io/artifactory/maven;build.timestamp=${new java.util.Date().getTime}"
  },
  publishMavenStyle  := true,
  versionScheme      := Some("semver-spec"),
)

val commonSettings = publishSettings ++ Seq(
  organization := "io.morgaroth",
  resolvers += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/",
  scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8"),
  logBuffered := false,
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
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
    name := "jira4s",
    publish := {},
    crossScalaVersions := crossScalaVersionsValues,
    releaseTagComment := s"Releasing ${(version in ThisBuild).value} [skip ci]",
    releaseCommitMessage := s"Setting version to ${(version in ThisBuild).value} [skip ci]",
    releaseNextCommitMessage := s"Setting version to ${(version in ThisBuild).value} [skip ci]",
    releaseCrossBuild := true,
  )
