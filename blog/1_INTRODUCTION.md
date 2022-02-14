## Intro
TODO write introduction about series of blog posts about telemetry in different Scala ecosystems
and APM products
Please, NOTE: We are not going to compare APM as whole products between each other. This series of posts focus on 
how to use each APM product in different Scala ecosystem on example of single service monitoring.


### Scala ecosystems
In series of articles we are going to review number of Application Performance Monitoring (APM) and telemetry solutions
which could be used in different Scala ecosystem.
In particular, two main ecosystem we are going to consider:
- Lightbend - mostly known for `akka` and surrounding projects, such as `akka-http`, `akka-streams` etc.;
- Typelevel - ecosystem build on top of `cats-effect` library, such as `fs2`, `doobie`, `http4s` etc.

# Telemetry and APM products
In particular, following telemetry solutions are considered:
- Splunk APM;
- Sentry APM;
- Datadog;
- Grafana;