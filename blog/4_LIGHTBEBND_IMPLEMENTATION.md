## Implementation details
Following libraries and frameworks were used to implement `tickets-service` in Lightbend ecosystem:
`akka-http` - for HTTP server and client;
`slick` - for database access layer, along with Postgre JDBC driver;
`elastic4s` - not really library supported by Lightbend, but it provides convenient Scala API, abstracted over 
underlying effect, hence can be used with `Future`, which is our case.
`kafka` - plain Java client to write records to `tickets`. Since, we don't need to read and process messages,
`akka-stream` is not used here.