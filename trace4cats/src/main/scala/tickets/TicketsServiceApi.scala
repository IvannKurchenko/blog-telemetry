package tickets

import cats.effect._
import com.comcast.ip4s._
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.ember.server._
import org.http4s.implicits._
import tickets.model.{CreateTicket, Ticket, UpdateTicket}

import scala.concurrent.duration._
import tickets.service.TicketsService

class TicketsServiceApi(service: TicketsService) {

  private implicit val createTicketDecoder = jsonOf[IO, CreateTicket]
  private implicit val updateTicketDecoder = jsonOf[IO, UpdateTicket]

  object SearchQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("search")

  object ProjectIdQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Long]("project")

  object ProjectIdPathMatcher {
    def unapply(str: String): Option[Long] = str.toLongOption
  }

  def app = HttpRoutes.of[IO] {
    case _@GET -> Root / "tickets" :? SearchQueryParamMatcher(search) +& ProjectIdQueryParamMatcher(project) =>
      for {
        tickets <- service.searchTickets(search, project)
        resp <- Ok(tickets.asJson)
      } yield resp

    case req@POST -> Root / "tickets" =>
      for {
        create <- req.as[CreateTicket]
        ticket  <- service.createTicket(create)
        resp <- Ok(ticket.asJson)
      } yield resp

    case req@PUT -> Root / "tickets" =>
      for {
        update <- req.as[UpdateTicket]
        ticket <- service.update(update)
        resp <- ticket.map(_.asJson).fold(NotFound(s"Ticket with id ${update.id} not found"))(Ok(_))
      } yield resp


    case _@PUT -> Root / "tickets" / ProjectIdPathMatcher(id) =>
      for {
        _ <- service.delete(id)
        resp <- Ok("")
      } yield resp

  }.orNotFound
}
