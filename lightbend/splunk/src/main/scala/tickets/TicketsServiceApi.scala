package tickets

import akka.http.scaladsl.server.Directives._
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._
import io.circe.syntax._

class TicketsServiceApi {

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
            parameter("project".as[Int].?) { projectId =>
              complete("")
            }
          }
          complete("")
        } ~ post {
          complete("")
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
