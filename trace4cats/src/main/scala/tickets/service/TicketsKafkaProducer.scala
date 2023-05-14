package tickets.service

import cats.effect._
import cats.syntax.all._
import fs2.kafka._
import io.circe.generic.auto._
import io.circe.syntax._
import org.typelevel.otel4s.trace.Tracer
import tickets.KafkaConfiguration
import tickets.model.{Ticket, TicketEvent}
import tickets.telemetry._

class TicketsKafkaProducer[F[_]: Async: Tracer](kafka: KafkaConfiguration) {

  private val producerSettings: ProducerSettings[F, String, TicketEvent] = ProducerSettings(
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
      producer.produceOneTraced(kafka.topic, event.ticket.id.toString, event)
    }
  }
}
