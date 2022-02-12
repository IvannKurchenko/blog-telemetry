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

  lazy val lightbendSpecific = {
    val akkaHttp =  "com.typesafe.akka" %% "akka-http" % "10.2.7"
    val akkaHttpCirce = "de.heikoseeberger" %% "akka-http-circe" % "1.39.2"
    val akkaStream =  "com.typesafe.akka" %% "akka-stream" % "2.6.18"
    val akkaTyped = "com.typesafe.akka" %% "akka-actor-typed" % "2.6.18"

    val slick = "com.typesafe.slick" %% "slick" % "3.3.3"
    val slickHikari = "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3"

    val kafka = "org.apache.kafka" %% "kafka" % "3.1.0"

    val kamonAkka = "io.kamon" %% "kamon-core" % "2.4.6"
    val kamonCore = "io.kamon" %% "kamon-core" % "2.4.6"

    val logging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4"
    val logback = "ch.qos.logback" % "logback-classic" % "1.2.10"

    Seq(
      akkaHttp,
      akkaHttpCirce,
      akkaStream,
      akkaTyped,

      slick,
      slickHikari,

      kafka,

      kamonAkka,
      kamonCore,

      logging,
      logback
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
