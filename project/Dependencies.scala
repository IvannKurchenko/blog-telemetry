import sbt._

object Dependencies {

  lazy val circe = {
    val circeVersion = "0.14.1"

    Seq(
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion
    )
  }

  lazy val pureConfig = {
    Seq("com.github.pureconfig" %% "pureconfig" % "0.17.1")
  }

  lazy val common = circe ++ pureConfig

  lazy val kamon = {
    val version = "2.4.6"
    Seq(
      "io.kamon" %% "kamon-core" % version,
      "io.kamon" %% "kamon-core" % version
    )
  }

  lazy val lightbendSpecific = {
    Seq(
      "com.typesafe.akka" %% "akka-http" % "10.2.7",
      "de.heikoseeberger" %% "akka-http-circe" % "1.39.2",
      "com.typesafe.akka" %% "akka-stream" % "2.6.18",
      "com.typesafe.akka" %% "akka-actor-typed" % "2.6.18",

      "com.typesafe.slick" %% "slick" % "3.3.3",
      "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3",
      "org.postgresql" % "postgresql" % "9.4-1206-jdbc42",
      "io.github.nafg.slick-migration-api" %% "slick-migration-api" % "0.8.2",

      "com.sksamuel.elastic4s" %% "elastic4s-client-esjava" % "7.17.0",
      "com.sksamuel.elastic4s" %% "elastic4s-json-circe" % "7.17.0",

      "org.apache.kafka" %% "kafka" % "3.1.0",

      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
      "ch.qos.logback" % "logback-classic" % "1.2.10"
    )
  }

  lazy val lightbend = lightbendSpecific ++ common

  lazy val gatling = {
    Seq(
      "io.gatling" % "gatling-core" % "3.7.4",
      "io.gatling" % "gatling-test-framework" % "3.7.4"
    )
  }
}
