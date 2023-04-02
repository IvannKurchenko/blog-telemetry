package tickets.service

import io.confluent.kafka.serializers.KafkaJsonSerializer
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import org.apache.kafka.common.serialization.StringSerializer
import tickets.KafkaConfiguration
import tickets.model.{Ticket, TicketEvent}

import java.util.Properties
import scala.concurrent.{ExecutionContext, Future}

class TicketsKafkaProducer(kafka: KafkaConfiguration)(implicit ec: ExecutionContext) {

  private val producer = {
    val settings = new Properties()

    settings.put(ProducerConfig.CLIENT_ID_CONFIG, "tickets.producer")
    settings.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.url)
    settings.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer])
    settings.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[KafkaJsonSerializer[TicketEvent]])
    settings.put(ProducerConfig.ACKS_CONFIG, "all")

    new KafkaProducer[String, TicketEvent](settings)
  }

  def sendCreated(ticket: Ticket): Future[Unit] = {
    send(TicketEvent("create", ticket)).map(_ => ())
  }


  def sendUpdated(ticket: Ticket): Future[Unit] = {
    send(TicketEvent("updated", ticket)).map(_ => ())
  }

  private def send(event: TicketEvent) = {
    val record = new ProducerRecord[String, TicketEvent](kafka.topic, event.ticket.id.toString, event)
    Future(producer.send(record).get())
  }

  def shutdown(): Unit = {
    producer.close()
  }
}
