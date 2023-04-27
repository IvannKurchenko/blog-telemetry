package tickets

import cats.data.Kleisli
import cats.effect._
import cats.implicits._
import io.circe.Json
import io.circe.generic.auto._
import io.circe.syntax._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import tickets.model.{CreateTicket, UpdateTicket}
import tickets.service.TicketsService

class TicketsServiceApi[F[_]](service: TicketsService[F])
                             (implicit F: Sync[F], A: Async[F], C: Concurrent[F]) {
  private val dsl = Http4sDsl[F]
  import dsl._

  private implicit val jsonEncoder: EntityEncoder[F, Json] = jsonEncoder[F]
  private implicit val createTicketDecoder: EntityDecoder[F, CreateTicket] = jsonOf[F, CreateTicket]
  private implicit val updateTicketDecoder: EntityDecoder[F, UpdateTicket] = jsonOf[F, UpdateTicket]

  object SearchQueryParamMatcher extends OptionalQueryParamDecoderMatcher[String]("search")

  object ProjectIdQueryParamMatcher extends OptionalQueryParamDecoderMatcher[Long]("project")

  object ProjectIdPathMatcher {
    def unapply(str: String): Option[Long] = str.toLongOption
  }

  def app: Kleisli[F, Request[F], Response[F]] = HttpRoutes.of[F] {
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
