## Intro
This series of articles aimed to show landscape of telemetry and APM solutions possible to use in different
Scala ecosystems or stacks if you will. I possibly won't cover all of them, but will try to cover main one.
Please, NOTE: this series is not going to compare APM as whole products between each other, instead it focuses on
how to use each APM product in different Scala ecosystem and perhaps highlight some pitfalls from this perspective only.

All source code can be found in [this repository](https://github.com/IvannKurchenko/blog-telemetry).

## Scala ecosystems
Following ecosystems are going to be considered:
- Lightbend - mostly known for `akka` and surrounding projects, such as `akka-http`, `akka-streams` etc.;
- Typelevel - ecosystem build on top of `cats-effect` library, such as `fs2`, `doobie`, `http4s` etc.

In this particular post, let's call it "Part 1", I will focus on `Lightbend` stack first.

## Telemetry and APM products
Following telemetry and APM solutions are considered:
- [Splunk APM](https://www.splunk.com/en_us/observability/apm-application-performance-monitoring.html);
- [Sentry APM](https://sentry.io/for/performance);
- [Datadog](https://www.datadoghq.com);
- Grafana - self-hosted Grafana application backed by some metrics store, such as InfluxDB;