package tickets

import akka.http.scaladsl.model.StatusCodes.NotFound
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._
import io.circe.syntax._
import io.circe.generic.auto._
import tickets.model.{CreateTicket, UpdateTicket}
import tickets.service.TicketsService

class TicketsServiceApi(service: TicketsService) {

  val route =
    pathPrefix("tickets") {
      pathEnd {
        get {
          parameter("search".?) { search =>
            parameter("project".as[Long].?) { projectId =>
              onSuccess(service.searchTickets(search, projectId)) { tickets =>
                complete(tickets.asJson)
              }
            }
          }
        } ~ post {
          entity(as[CreateTicket]) { create =>
            onSuccess(service.createTicket(create)) { ticket =>
              complete(ticket.asJson)
            }
          }
        } ~ put {
          entity(as[UpdateTicket]) { update =>
            onSuccess(service.update(update)) {
              case Some(ticket) => complete(ticket.asJson)
              case None => complete(HttpResponse(NotFound.intValue, entity = s"Ticket with id ${update.id} not found"))
            }
          }
        }
      } ~ path(LongNumber) { id =>
        delete {
          onSuccess(service.delete(id))(_ => complete(""))
        }
      }
    }
}
