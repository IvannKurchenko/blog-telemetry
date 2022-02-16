## Splunk APM
Splunk APM product natively supports [OpenTelemetry](https://opentelemetry.io) for instrumentation.
Unfortunately, it has no dedicated support for variety of Scala libraries, but luckily it some supports plenty 
instrumentation for plenty of JVM libs. For ticket-service case, following instrumentation is considered: 
- [Akka HTTP](https://doc.akka.io/docs/akka-http/current/index.html)
- [JDBC](https://docs.oracle.com/javase/8/docs/api/java/sql/package-summary.html)
- [Elasticsearch Client](https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/index.html)
- [Kafka Producer/Consumer API](https://kafka.apache.org/documentation/#producerapi)

_Despite the fact list pretty long, I strongly encourage you to check if it fits your particular needs._ 

TODO: 
how to plug
telemetry results (screenshot?)
Conclussion: pros and cons.

Links:
- [OpenTelemetry JVM instrumentation](https://opentelemetry.io/docs/instrumentation/java/)
- [OpenTelemetry supported JVM libraries](https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/docs/supported-libraries.md)
- [Splunk APM agent docker instalation](https://docs.splunk.com/Observability/gdi/opentelemetry/install-linux.html#linux-docker)
