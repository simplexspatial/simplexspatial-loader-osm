name := "simplexspatial-loader-osm"

version := "0.1-SNAPSHOT"

scalaVersion := "2.12.11"

lazy val simplexspatialVersion = "0.0.1-SNAPSHOT"
lazy val akkaVersion = "2.6.4"

organization := "com.simplexportal.spatial"
organizationHomepage := Some(url("http://www.simplexportal.com"))
organizationName := "SimplexPortal Ltd"
maintainer := "angelcervera@simplexportal.com"
developers := List(
  Developer(
    "angelcervera",
    "Angel Cervera Claudio",
    "angelcervera@simplexportal.com",
    url("http://github.com/angelcervera")
  )
)
startYear := Some(2019)
licenses += ("Apache-2.0", new URL(
  "https://www.apache.org/licenses/LICENSE-2.0.txt"
))

resolvers += "osm4scala repo" at "https://dl.bintray.com/angelcervera/maven"
scalacOptions ++= Seq("-deprecation", "-feature")

lazy val scalatestVersion = "3.1.1"

enablePlugins(JavaAppPackaging)
enablePlugins(UniversalPlugin)
enablePlugins(BashStartScriptPlugin)
enablePlugins(LauncherJarPlugin)
enablePlugins(JavaAgent) // ALPN agent

name := "simplexspatial-loader-osm"
description := "OSM Loader"
packageDescription := "SimplexSpatial OpenStreetMap Data Loader"
javaAgents += "org.mortbay.jetty.alpn" % "jetty-alpn-agent" % "2.0.9" % "runtime;test"
mainClass in (Compile, packageBin) := Some(
  "com.simplexportal.spatial.loadosm.Main"
)

libraryDependencies ++= Seq(
  "com.acervera.osm4scala" %% "osm4scala-core" % "1.0.1",
  "com.simplexportal.spatial" %% "grpc-client-scala" % simplexspatialVersion,
  "com.github.scopt" %% "scopt" % "3.7.1",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.akka" %% "akka-discovery" % akkaVersion,
  "com.fasterxml.jackson.module" % "jackson-module-scala_2.12" % "2.10.3",
  "io.tmos" %% "arm4s" % "1.1.0",
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % scalatestVersion % Test,
  "org.scalactic" %% "scalactic" % scalatestVersion % Test
)
