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

scalaVersion := "2.12.11"

enablePlugins(JavaAppPackaging)
enablePlugins(BashStartScriptPlugin)
enablePlugins(LauncherJarPlugin)
enablePlugins(JavaAgent) // ALPN agent
enablePlugins(AkkaGrpcPlugin)
version := "0.1-SNAPSHOT"
name := "simplexspatial-loader-osm"
description := "OSM Loader"
packageDescription := "SimplexSpatial OpenStreetMap Data Loader"
javaAgents += "org.mortbay.jetty.alpn" % "jetty-alpn-agent" % "2.0.9" % "runtime;test"
scalacOptions ++= Seq("-deprecation", "-feature")
mainClass in (Compile, packageBin) := Some(
  "com.simplexportal.spatial.loadosm.Main"
)


//PB.protoSources in Compile += (resourceDirectory in (protobufApi, Compile)).value,
akkaGrpcGeneratedSources := Seq(AkkaGrpc.Client)
akkaGrpcGeneratedLanguages := Seq(AkkaGrpc.Scala)

lazy val scalatestVersion = "3.1.1"
lazy val simplexspatialVersion = "0.0.1-SNAPSHOT"
lazy val akkaVersion = "2.6.5"
lazy val osm4scalaVersion = "1.0.3"
lazy val scoptVersion = "3.7.1"
lazy val logbackVersion = "1.2.3"
lazy val sparkVersion = "2.4.5"
lazy val arm4sVersion = "1.1.0"
lazy val betterFilesVersion = "3.8.0"
lazy val jacksonVersion = "2.10.3"

libraryDependencies ++= Seq(
  "com.acervera.osm4scala" %% "osm4scala-core" % "1.0.4",
  "com.simplexportal.spatial" % "protobuf-api" % simplexspatialVersion % "protobuf-src",
  "com.github.scopt" %% "scopt" % scoptVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "com.typesafe.akka" %% "akka-discovery" % akkaVersion,
  "com.fasterxml.jackson.module" % "jackson-module-scala_2.12" % jacksonVersion,
  "io.tmos" %% "arm4s" % arm4sVersion,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % scalatestVersion % Test,
  "org.scalactic" %% "scalactic" % scalatestVersion % Test
)
