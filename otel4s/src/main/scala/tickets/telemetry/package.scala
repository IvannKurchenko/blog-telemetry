package tickets

package object telemetry
  extends ClientMiddleware
    with ServerMiddleware
    with ElasticMiddleware
    with KafkaProducerMiddleware


