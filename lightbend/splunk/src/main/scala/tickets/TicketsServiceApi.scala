package tickets

import akka.http.scaladsl.server.Directives._
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._
import io.circe.syntax._
import io.circe.generic.auto._

class TicketsServiceApi(service: TicketsService) {

  /*
  GET /v1/tickets?search={search-query}&project={} - performs full text search over all tickets
  POST /v1/tickets - create single ticket
  PUT /v1/tickets/:id - update single ticket
  DELETE /v1/tickets/:id - delete single ticket
   */

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
        }
      } ~
        path(LongNumber) { id =>
          pathEnd {
            put {
              complete("")
            } ~ delete {
              complete("")
            }
          }
        }
    }
}
