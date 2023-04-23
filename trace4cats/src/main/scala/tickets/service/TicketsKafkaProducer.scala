package tickets.service

import cats.effect.*
import cats.syntax.all.*
import fs2.kafka.*
import tickets.KafkaConfiguration
import tickets.model.{Ticket, TicketEvent}
import io.circe.*
import io.circe.Decoder.Result
import io.circe.syntax.*
import io.circe.generic.auto.*

import scala.concurrent.duration.*

class TicketsKafkaProducer(kafka: KafkaConfiguration) {
  val a: Either[ParsingFailure, Result[TicketEvent]] = parser.parse("").map(_.as[TicketEvent])

  private val producerSettings = ProducerSettings(
    keySerializer = Serializer[IO, String],
    valueSerializer = Serializer[IO, String].contramap[TicketEvent](_.asJson.noSpaces)
  ).withBootstrapServers(kafka.url)

  def sendCreated(ticket: Ticket): IO[Unit] = {
    send(TicketEvent("create", ticket)).void
  }


  def sendUpdated(ticket: Ticket): IO[Unit] = {
    send(TicketEvent("updated", ticket)).void
  }

  private def send(event: TicketEvent) = {
    KafkaProducer.resource(producerSettings).use { producer =>
      producer.produceOne(kafka.topic, event.ticket.id.toString, event)
    }
  }
}
