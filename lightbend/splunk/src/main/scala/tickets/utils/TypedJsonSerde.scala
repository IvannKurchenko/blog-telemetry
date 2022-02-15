package tickets.utils

import io.confluent.kafka.serializers.{KafkaJsonDeserializer, KafkaJsonSerializer}
import org.apache.kafka.common.serialization.{Serde, Serdes}

import java.util
import scala.reflect.ClassTag

object TypedJsonSerde {
  def serde[T](implicit tag: ClassTag[T]): Serde[T] = {
    val properties = new util.HashMap[String, AnyRef]
    properties.put("json.value.type", tag.runtimeClass)

    val serializer = new KafkaJsonSerializer[T]
    serializer.configure(properties, false)

    val deserializer = new KafkaJsonDeserializer[T]
    deserializer.configure(properties, false)

    Serdes.serdeFrom(serializer, deserializer)
  }
}

