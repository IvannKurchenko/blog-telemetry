package tickets.service

import cats.effect._
import cats.syntax.all._
import fs2.kafka._
import io.circe.generic.auto._
import io.circe.syntax._
import tickets.KafkaConfiguration
import tickets.model.{Ticket, TicketEvent}

class TicketsKafkaProducer[F[_]](kafka: KafkaConfiguration)(implicit A: Async[F]) {

  private val producerSettings = ProducerSettings(
    keySerializer = Serializer[F, String],
    valueSerializer = Serializer[F, String].contramap[TicketEvent](_.asJson.noSpaces)
  ).withBootstrapServers(kafka.url)

  def sendCreated(ticket: Ticket): F[Unit] = {
    send(TicketEvent("create", ticket)).void
  }


  def sendUpdated(ticket: Ticket): F[Unit] = {
    send(TicketEvent("updated", ticket)).void
  }

  private def send(event: TicketEvent): F[ProducerResult[String, TicketEvent]] = {
    KafkaProducer.resource(producerSettings).use { producer =>
      producer.produceOne(kafka.topic, event.ticket.id.toString, event).flatten
    }
  }
}
