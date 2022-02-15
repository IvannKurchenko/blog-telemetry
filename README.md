# Blog: Telemetry in Scala

# Blog
This repository contains source code for "Telemetry in Scala" blog series. 

# Structure
Source code subprojects organised using following way: `{stack-name}/{monitoring-system}`
In particular:
```
/ load_testing - Gatling tests to simulate user traffic
/ lightbend - System implementation using mostly "Lightbend" stack: akka, slick.
    / splunk - using Splunk APM for as main monitoring tool
    / sentry - using Sentry APM for as main monitoring tool
```

# How to run
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
TODO
```

After, run load testing to simulate user traffic 
```
TODO
```

Check target APM or any monitoring tool and verify telemetry has been sent. 

Stop environment using docker compose:
```
docker-compose -f docker/environment-docker-compose.yml down
```