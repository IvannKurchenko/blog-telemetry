import sbt._

object Dependencies {

  lazy val circe = {
    val circeVersion = "0.14.5"

    Seq(
      "io.circe" %% "circe-core" % circeVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "io.circe" %% "circe-parser" % circeVersion
    )
  }

  lazy val pureConfig = {
    Seq("com.github.pureconfig" %% "pureconfig" % "0.17.2")
  }

  lazy val common = circe ++ pureConfig

  lazy val kamon = {
    val version = "2.6.0"
    Seq(
      "io.kamon" %% "kamon-core" % version,
      "io.kamon" %% "kamon-core" % version
    )
  }

  lazy val lightbendSpecific = {
    val kafkaVersion = "3.4.0"
    val elastic4sVersion = "8.6.0"
    val slickVersion = "3.4.1"
    val akkaVersion = "2.8.0"

    Seq(
      "com.typesafe.akka" %% "akka-http" % "10.5.0",
      "de.heikoseeberger" %% "akka-http-circe" % "1.39.2",
      "com.typesafe.akka" %% "akka-stream" % akkaVersion,
      "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,

      "com.typesafe.slick" %% "slick" % slickVersion,
      "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,

      "org.postgresql" % "postgresql" % "42.6.0",
      "org.flywaydb" % "flyway-core" % "9.16.1",

      "com.sksamuel.elastic4s" %% "elastic4s-client-esjava" % elastic4sVersion,
      "com.sksamuel.elastic4s" %% "elastic4s-json-circe" % elastic4sVersion,

      "org.apache.kafka" %% "kafka" % kafkaVersion,
      "org.apache.kafka" % "kafka-clients" % kafkaVersion,
      "io.confluent" % "kafka-json-serializer" % "7.3.3",

      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
      "ch.qos.logback" % "logback-classic" % "1.4.6"
    )
  }

  lazy val openTelemetrySpecific = {
    val version = "1.24.0"
    val alphaVersion =  s"$version-alpha"
    Seq(
      "io.opentelemetry" % "opentelemetry-bom" % version pomOnly(),
      "io.opentelemetry" % "opentelemetry-api" % version,
      "io.opentelemetry" % "opentelemetry-sdk" % version,
      "io.opentelemetry" % "opentelemetry-exporter-jaeger" % version,
      "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % alphaVersion,
      "io.opentelemetry" % "opentelemetry-exporter-prometheus" % alphaVersion,
      "io.opentelemetry" % "opentelemetry-exporter-zipkin" % version,
      "io.opentelemetry" % "opentelemetry-exporter-jaeger" % version,
      "io.opentelemetry" % "opentelemetry-exporter-otlp" % version,

      "io.opentelemetry.javaagent" % "opentelemetry-javaagent" % version % "runtime"
    )
  }

  lazy val openTelemetry = openTelemetrySpecific ++ lightbendSpecific ++ common

  lazy val lightbend = lightbendSpecific ++ common

  lazy val gatling = {
    val gatlingVersion = "3.9.2"
    Seq(
      "io.gatling" % "gatling-core" % gatlingVersion,
      "io.gatling" % "gatling-test-framework" % gatlingVersion,
      "io.gatling.highcharts" % "gatling-charts-highcharts" % gatlingVersion
    )
  }

  lazy val loadTesting = circe ++ gatling
}
