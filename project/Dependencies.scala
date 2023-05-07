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

  lazy val elastic4s = {
    val elastic4sVersion = "8.6.0"
    Seq(
      "com.sksamuel.elastic4s" %% "elastic4s-client-esjava" % elastic4sVersion,
      "com.sksamuel.elastic4s" %% "elastic4s-json-circe" % elastic4sVersion
    )
  }

  lazy val flyway = {
    Seq(
      "org.postgresql" % "postgresql" % "42.6.0",
      "org.flywaydb" % "flyway-core" % "9.16.1",
    )
  }

  lazy val kafka = {
    val kafkaVersion = "3.4.0"

    Seq(
      "org.apache.kafka" % "kafka-clients" % kafkaVersion,
      "io.confluent" % "kafka-json-serializer" % "7.3.3",
    )
  }

  lazy val common = circe ++ pureConfig ++ elastic4s ++ flyway ++ kafka

  lazy val lightbendSpecific = {
    val slickVersion = "3.4.1"
    val akkaVersion = "2.8.0"

    Seq(
      "com.typesafe.akka" %% "akka-http" % "10.5.0",
      "de.heikoseeberger" %% "akka-http-circe" % "1.39.2",
      "com.typesafe.akka" %% "akka-stream" % akkaVersion exclude("org.scala-lang.modules", "scala-java8-compat_2.12"),
      "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion exclude("org.scala-lang.modules", "scala-java8-compat_2.12"),

      "com.typesafe.slick" %% "slick" % slickVersion,
      "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,


      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
      "ch.qos.logback" % "logback-classic" % "1.4.6"
    )
  }

  lazy val openTelemetrySpecific = {
    val version = "1.24.0"
    val alphaVersion = s"$version-alpha"
    Seq(
      "io.opentelemetry" % "opentelemetry-bom" % version pomOnly(),
      "io.opentelemetry" % "opentelemetry-api" % version,
      "io.opentelemetry" % "opentelemetry-sdk" % version,
      "io.opentelemetry" % "opentelemetry-sdk-extension-autoconfigure" % alphaVersion,
      "io.opentelemetry" % "opentelemetry-exporter-prometheus" % alphaVersion,
      "io.opentelemetry" % "opentelemetry-exporter-zipkin" % version,
      //"io.opentelemetry" % "opentelemetry-exporter-otlp" % version,

      "io.opentelemetry.javaagent" % "opentelemetry-javaagent" % version % "runtime"
    )
  }

  lazy val kamonSpecific = {
    lazy val kamonVersion = "2.6.0"
    Seq(
      "io.kamon" %% "kamon-core" % kamonVersion,
      "io.kamon" %% "kamon-bundle" % kamonVersion, //includes all instrumentation's,

      "io.kamon" %% "kamon-prometheus" % kamonVersion,
      "io.kamon" %% "kamon-zipkin" % kamonVersion,
      "io.kamon" %% "kamon-apm-reporter" % kamonVersion
    )
  }

  lazy val catsEffectSpecific = {
    val catsEffectVersion = "3.4.8"
    val http4sVersion = "0.23.14"
    val doobieVersion = "1.0.0-RC1"
    val log4catsVersion = "2.5.0"
    val fs2Version = "3.0.0"

    Seq(
      "org.typelevel" %% "cats-effect" % catsEffectVersion,

      "org.http4s" %% "http4s-dsl" % http4sVersion,
      "org.http4s" %% "http4s-core" % http4sVersion,
      "org.http4s" %% "http4s-client" % http4sVersion,
      "org.http4s" %% "http4s-server" % http4sVersion,
      "org.http4s" %% "http4s-circe" % http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % http4sVersion,
      "org.http4s" %% "http4s-blaze-server" % http4sVersion,

      "org.tpolecat" %% "doobie-core" % doobieVersion,

      "org.tpolecat" %% "doobie-hikari" % doobieVersion, // HikariCP transactor.
      "org.tpolecat" %% "doobie-postgres" % doobieVersion, // Postgres driver 42.3.1 + type mappings.

      "com.github.fd4s" %% "fs2-kafka" % fs2Version,

      "org.typelevel" %% "otel4s-java" % "0.2.1",

      "org.typelevel" %% "log4cats-core" % log4catsVersion,
      "org.typelevel" %% "log4cats-slf4j" % log4catsVersion,
      "ch.qos.logback" % "logback-classic" % "1.4.6"
    )
  }

  lazy val openTelemetry = openTelemetrySpecific ++ lightbendSpecific ++ common

  lazy val kamon = kamonSpecific ++ lightbendSpecific ++ common

  lazy val lightbend = lightbendSpecific ++ common

  lazy val trace4cats = catsEffectSpecific ++ openTelemetrySpecific ++ common

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
