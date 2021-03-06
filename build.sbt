scalaVersion := "2.13.3"

name := "ahql"

ThisBuild / organization := "io.github.d-exclaimation"
ThisBuild / version := "0.2.4"
ThisBuild / organizationHomepage := Some(url("https://www.dexclaimation.com"))
ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/d-exclaimation/ahql"),
    "scm:git@github.d-exclaimation/ahql.git"
  )
)

ThisBuild / developers := List(
  Developer(
    id = "d-exclaimation",
    name = "Vincent",
    email = "thisoneis4business@gmail.com",
    url = url("https://www.dexclaimation.com")
  )
)

ThisBuild / description := "Akka Http Query Library, a minimal GraphQL client and server exposing as a set of akka-http utilities"
ThisBuild / licenses += ("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))
ThisBuild / homepage := Some(url("https://github.com/d-exclaimation/ahql"))

// Remove all additional repository other than Maven Central from POM
ThisBuild / pomIncludeRepository := { _ => false }

ThisBuild / publishTo := {
  val nexus = "https://s01.oss.sonatype.org/"
  if (isSnapshot.value) Some("snapshots" at nexus + "content/repositories/snapshots")
  else Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

ThisBuild / publishMavenStyle := true

ThisBuild / versionScheme := Some("early-semver")

libraryDependencies ++= {
  val sangriaVer = "2.1.5"
  val AkkaVersion = "2.6.17"
  val AkkaHttpVersion = "10.2.7"
  val sangriaSprayVer = "1.0.2"

  Seq(
    "org.sangria-graphql" %% "sangria" % sangriaVer,
    "org.sangria-graphql" %% "sangria-spray-json" % sangriaSprayVer,
    "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
    "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
    "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
    "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion % Test,
    "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion % Test,
    "org.scalatest" %% "scalatest" % "3.2.9" % Test,
  )
}
