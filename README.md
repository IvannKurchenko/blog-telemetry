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
```

Run environment using docker compose:
```
docker-compose -f docker/environment-docker-compose.yml up -d
```

Then run tickets service on particular stack and using particular telemetry, for instance:
```
sbt run lightbend/splunk
```

After, run load testing to simulate user traffic 
```
sbt Gatling/test
```

Check target APM or any monitoring tool and verify telemetry has been sent. 

Stop environment using docker compose:
```
docker-compose -f docker/environment-docker-compose.yml down
```

## APM 

### Splunk
In order to use Splunk APM locally, use `docker/splunk-apm-docker-compose.yml` docker-compose.
Replace `SPLUNK_ACCESS_TOKEN` and `SPLUNK_REALM` with yours and  run agent before app launch.
