name := "blog-telemetry"
version := "0.1"


lazy val load_testing =
  (project in file("load_testing")).
    enablePlugins(GatlingPlugin).
    settings(
      scalaVersion := "2.13.1",
      libraryDependencies := Dependencies.loadTesting
    )

lazy val projects_service =
  (project in file("projects_service")).
    enablePlugins(JavaAppPackaging, DockerPlugin).
    settings(
      scalaVersion := "2.13.1",
      resolvers += "confluent" at "https://packages.confluent.io/maven/",
      libraryDependencies := Dependencies.lightbend,
      dockerExposedPorts := Seq(10000),
      Docker / packageName  := "projects_service",
      Docker / version := "latest"
    )

lazy val opentelemetry =
  (project in file("opentelemetry")).
    enablePlugins(JavaAgent, JavaAppPackaging, DockerPlugin).
    settings(
      scalaVersion := "2.13.1",
      resolvers += "confluent" at "https://packages.confluent.io/maven/",
      libraryDependencies := Dependencies.lightbend,
      javaAgents += "io.opentelemetry.javaagent" % "opentelemetry-javaagent" % "1.11.0"
    )

