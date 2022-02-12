name := "blog-telemetry"
version := "0.1"


lazy val load_testing =
  (project in file("load_testing")).
    settings(
      scalaVersion := "2.13.1",
      libraryDependencies := Dependencies.gatling
    )

lazy val projects_service =
  (project in file("stub_projects_service")).
    settings(
      scalaVersion := "2.13.1",
      libraryDependencies := Dependencies.gatling
    )

lazy val lightbend_splunk_tickets_service =
  (project in file("lightbend/splunk/tickets_service")).
    settings(
      scalaVersion := "2.13.1",
      libraryDependencies := Dependencies.lightbend
    )

lazy val lightbend_sentry_tickets_service =
  (project in file("lightbend/sentry/tickets_service")).
    settings(
      scalaVersion := "2.13.1",
      libraryDependencies := Dependencies.lightbend
    )
