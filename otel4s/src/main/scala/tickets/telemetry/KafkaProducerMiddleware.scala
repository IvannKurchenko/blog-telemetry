package tickets.telemetry

import cats.effect.{Async, Sync}
import cats.implicits._
import fs2.kafka.{KafkaProducer, ProducerResult}
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.trace.Tracer

trait KafkaProducerMiddleware {
  implicit class KafkaProducerMiddlewareOps[F[_] : Sync : Async : Tracer, K, V](producer: KafkaProducer[F, K, V]) {
    def produceOneTraced(topic: String, key: K, value: V): F[ProducerResult[K, V]] = {
      Tracer[F].span("kafka.produce", Attribute("kafka.topic", topic))
        .surround(producer.produceOne(topic, key, value).flatten)
    }
  }
}

object KafkaProducerMiddleware extends KafkaProducerMiddleware