# Blog: Telemetry in Scala

## Blog
This repository contains source code for "Telemetry in Scala" blog series. 

## Layout

### Subprojects
Main source code divided on following subprojects:
```
/ projects_service - stub projects service
/ load_testing - Gatling tests to simulate user traffic
/ opentelemetry - System implementation using mostly "Lightbend" stack: akka, slick and Opentelemetry for instrumentation.
/ kamon - System implementation using mostly "Lightbend" stack: akka, slick and Kamon for instrumentation.
```

### Misc
Apart from source code current repo has the following folders:
```
blog - blog post
docker - various docker compose  
```

## How to run
In order to run system 

Build necessary docker containers:
```
sbt projects_service/docker:publishLocal
sbt opentelemetry/docker:publishLocal
sbt kamon/docker:publishLocal
sbt trace4cats/docker:publishLocal
```

Run specific system setup using docker compose. For instance :
```
docker-compose -f docker-compose/opentelemetry/prometheus-docker-compose.yml up -d
```

After, run load testing to simulate user traffic 
```
sbt Gatling/test
```

Check target APM or any monitoring tool and verify telemetry has been sent. 

Stop environment using docker compose:
```
docker-compose -f docker-compose/opentelemetry/prometheus-docker-compose.yml down
```
