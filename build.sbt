name := "blog-telemetry"
version := "0.1"


lazy val load_testing =
  (project in file("load_testing")).
    settings(
      scalaVersion := "2.13.1",
      libraryDependencies := Dependencies.gatling
    )

/*
 * Lightbend + Splunk
 */
lazy val lightbend_splunk_tickets_service =
  (project in file("lightbend/splunk/tickets_service")).
    settings(
      scalaVersion := "2.13.1",
      libraryDependencies := Dependencies.lightbend
    )

lazy val lightbend_splunk_users_service =
  (project in file("lightbend/splunk/users_service")).
    settings(
      scalaVersion := "2.13.1",
      libraryDependencies := Dependencies.lightbend
    )

lazy val lightbend_splunk_notifications_service =
  (project in file("lightbend/splunk/notifications_service")).
    settings(
      scalaVersion := "2.13.1",
      libraryDependencies := Dependencies.lightbend
    )

/*
 * Lightbend + Sentry
 */
lazy val lightbend_sentry_tickets_service =
  (project in file("lightbend/sentry/tickets_service")).
    settings(
      scalaVersion := "2.13.1",
      libraryDependencies := Dependencies.lightbend
    )

lazy val lightbend_sentry_users_service =
  (project in file("lightbend/sentry/users_service")).
    settings(
      scalaVersion := "2.13.1",
      libraryDependencies := Dependencies.lightbend
    )

lazy val lightbend_sentry_notifications_service =
  (project in file("lightbend/sentry/notifications_service")).
    settings(
      scalaVersion := "2.13.1",
      libraryDependencies := Dependencies.lightbend
    )