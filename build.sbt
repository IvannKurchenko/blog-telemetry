import com.typesafe.sbt.packager.docker.DockerChmodType

name := "blog-telemetry"
version := "0.1"


lazy val load_testing =
  (project in file("load_testing")).
    enablePlugins(GatlingPlugin).
    settings(
      scalaVersion := "2.13.10",
      libraryDependencies := Dependencies.loadTesting
    )

lazy val projects_service =
  (project in file("projects_service")).
    enablePlugins(JavaAppPackaging, DockerPlugin).
    settings(
      scalaVersion := "2.13.10",

      resolvers += "confluent" at "https://packages.confluent.io/maven/",
      libraryDependencies := Dependencies.lightbend,

      dockerExposedPorts := Seq(10000),
      dockerBaseImage := "openjdk:17",
      Docker / packageName := "projects_service",
      Docker / version := "latest"
    )

lazy val opentelemetry =
  (project in file("opentelemetry")).
    enablePlugins(JavaAgent, JavaAppPackaging, DockerPlugin).
    settings(
      scalaVersion := "2.13.10",

      resolvers += "confluent" at "https://packages.confluent.io/maven/",
      libraryDependencies := Dependencies.openTelemetry,

      javaAgents += "io.opentelemetry.javaagent" % "opentelemetry-javaagent" % "1.11.0",
      javaOptions += "-Dotel.javaagent.debug=true",

      mainClass := Some("tickets.TicketsServiceApplication"),

      dockerExposedPorts := Seq(10000),
      dockerBaseImage := "openjdk:17",
      Docker / packageName := "tickets_service_otel",
      Docker / version := "latest",
      Docker / dockerChmodType := DockerChmodType.UserGroupWriteExecute
    )


lazy val kamon =
  (project in file("kamon")).
    enablePlugins(JavaAgent, JavaAppPackaging, DockerPlugin).
    settings(
      scalaVersion := "2.13.10",

      resolvers += "confluent" at "https://packages.confluent.io/maven/",
      libraryDependencies := Dependencies.kamon,

      javaAgents += "io.kamon" % "kanela-agent" % "1.0.17",

      mainClass := Some("tickets.TicketsServiceApplication"),

      dockerExposedPorts := Seq(10000),
      dockerBaseImage := "openjdk:17",
      Docker / packageName := "tickets_service_kamon",
      Docker / version := "latest",
      Docker / dockerChmodType := DockerChmodType.UserGroupWriteExecute
    )


lazy val trace4cats =
  (project in file("trace4cats")).
    enablePlugins(JavaAgent, JavaAppPackaging, DockerPlugin).
    settings(
      scalaVersion := "2.13.10",

      resolvers += "confluent" at "https://packages.confluent.io/maven/",
      libraryDependencies := Dependencies.trace4cats,

      javaAgents += "io.opentelemetry.javaagent" % "opentelemetry-javaagent" % "1.24.0",
      javaOptions += "-Dotel.javaagent.debug=true",
      javaOptions += "-Dotel.java.global-autoconfigure.enabled=true",

      mainClass := Some("tickets.TicketsServiceApplication"),

      dockerExposedPorts := Seq(10000),
      dockerBaseImage := "openjdk:17",
      Docker / packageName := "tickets_service_otel4s",
      Docker / version := "latest",
      Docker / dockerChmodType := DockerChmodType.UserGroupWriteExecute
    )
