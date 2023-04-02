# Telemetry with Scala, part 2: Kamon
![](images/diagram_kamon.drawio.png)


## Previous part recap
TODO: We're monitoring ticketing micro-service , tickets count metrics and spans
User traffic 

![](images/diagram_system_architecture.drawio.png)

## Kamon
### Overview:

TODO
    - description supported exports (zipkin, Jaeger etc),
    - supported instrumentation: Play, Lagom, Akka
    - Possible APM solutions to plug

Similarly to OpenTelemetry, Kamon has also concepts of [Tracing and Metrics](https://kamon.io/docs/latest/core/#core-apis). 
Additionally, Kamon can wire additional context, which might to debug certain trace. 

Out of the box, Kamon provides instrumentation's like: [Akka HTTP](https://kamon.io/docs/latest/guides/installation/akka-http/), [Spring Boot](https://kamon.io/docs/latest/guides/installation/spring-boot/), [Play Framework](https://kamon.io/docs/latest/guides/installation/play-framework/) and [Lagom](https://kamon.io/docs/latest/guides/installation/lagom-framework/).

The full list of supported instrumentation's can be found [here](https://kamon.io/docs/latest/instrumentation/)

### How to plug for our application
We need Akka instrumentation described [here](https://kamon.io/docs/latest/guides/installation/akka-http/)
 TODO - ADD DEPENDENCIES

NOTE: At the moment of writing this blog post, used version of Kamon is 2.6.0.

Add instrumentation to build.sbt [Using sbt-native-packager](https://kamon.io/docs/latest/guides/how-to/start-with-the-kanela-agent/#using-sbt-native-packager) 
so it can enable the agent at runtime.

```scala

Add to `build.sbt`:
```scala
"io.kamon" %% "kamon-core" % "2.5.9"
```

### Metrics
TODO : CHECK METRICS KAMON HAVE AND COMPARE WITH OPENTELEMETRY!!!!

to track number of tickets we can use Kamon Gauge:
```scala
val ticketsGauge: Gauge = Kamon.gauge("tickets_count", "Total number of tickets")
```

Kamon has tags to provide additional information about metric, similarly to OpenTelemetry's Java API [Attributes](https://javadoc.io/doc/io.opentelemetry/opentelemetry-api/latest/io/opentelemetry/api/common/Attributes.html).
So to track number of tickets per project we can use following code:
```scala
ticketsGauge.withTag("project_id", ticket.project).increment()
```

### Metrics example: Prometheus
TODO : CONFIGURE PROMETHEUS
https://kamon.io/docs/latest/reporters/prometheus/

Add to configuration (`application.conf`):
```hocon
kamon { 
  prometheus {
    include-environment-tags = true
    embedded-server {
      hostname = 0.0.0.0
      port = 9094
    }
  }
}
```

![screenshot_kamon_prometheus.png](images/screenshot_kamon_prometheus.png)

### Tracing



### Tracing example: Zipkin

CHECK CONTEXT STUFF!!!!
https://kamon.io/docs/latest/core/context/#context


Request tracing with Akka-http
https://kamon.io/docs/latest/instrumentation/akka-http/#request-tracing
see if there is `X-Request-ID` in response headers and if it presents in spans

https://kamon.io/docs/latest/reporters/zipkin/

```hocon
kamon {
    zipkin {
      host = "localhost"
      port = 9411
      protocol = "http"
    }
}
```

In response header you can find
trace-id - `1b05566f51be20ad`

![screenshot_kamon_zipkin.png](images/screenshot_kamon_zipkin.png)

### APM Example: Datadog

### APM Example: Kamon APM

### Conclusion: 

Conclusion
couple words, pros, cons, compare with OpenTelemetry
todo: take a look at metrics and especially tracing - how different they are

Very hard to choose between OpenTelemetry and Kamon, as they provide similar functionality in very handy way.
OpenTelemetry is more mature, but Kamon is more lightweight and has more integrations.

TODO - FIGURE OUT WHAT ARE REAL BENEFITS OF USING KAMON OVER OPENTELEMETRY AND VICE VERSA!!!!
Idea: Kamon more Scala native and provide instrumentations with API's for Scala related stuff like Akka, futures and executors better than OpenTelemetry.
But Opentelemetry's contrlib certainly is more reach. (todo - add links to contribs)

TODO add 
See futures instrumentation in Kamon: https://kamon.io/docs/latest/instrumentation/futures/#futures-instrumentation

Conclusion
If you already using Opentelemetry across your organization, then it's probably better to stick with it, as it's language agnostic.
But, if Scala and Akka ecosystem is your primary choice Kamon might be better fit for you.


Another pitfall of Kamon is limited number of [supported reporters](https://kamon.io/docs/latest/reporters/). While OpenTelemetry is vendor-agnostic by design.  


- Next about TypeLevel 
