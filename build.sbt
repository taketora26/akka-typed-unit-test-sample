name := "akka-typed-unit-test-sample"

version := "1.0"

scalaVersion := "2.13.8"
lazy val macwireVersion = "2.5.0"
lazy val mockitoScalaVersion = "1.16.25"
lazy val akkaVersion = "2.6.18"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "ch.qos.logback" % "logback-classic" % "1.2.10",
  "com.softwaremill.macwire" % "macros_2.13" % macwireVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "org.scalatest" %% "scalatest" % "3.1.0" % Test,
  "org.mockito" %% "mockito-scala" % mockitoScalaVersion % Test
)
