package tickets

import akka.http.scaladsl.server.Directives._
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._
import io.circe.syntax._

class ProjectsApi {

  private val users = List("john.doe@acme.come", "john.doe.jr@acme.come", "john.doe.sr@acme.come")

  private val projects: Map[Long, Project] = (1L to 100L).map { id =>
    id -> Project(
       id = id,
       name = s"Stub project #$id",
       description = s"Stub project #$id",
       users = users,
       createdAt = System.currentTimeMillis(),
       createdBy = users.head,
       modifiedAt = System.currentTimeMillis(),
       modifiedBy = users.head
     )
  }.toMap

  val route =
    pathPrefix("projects") {
      path(LongNumber) { id =>
        pathEnd {
          get {
            projects.get(id).fold(complete(s"Not found project with id $id"))(project => complete(project.asJson))
          }
        }
      }
    }
}
