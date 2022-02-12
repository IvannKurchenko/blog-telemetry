package tickets

import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._
import io.circe.syntax._

class ProjectsApi(servise: ProjectsService) {

  val route =
    pathPrefix("v1" / "projects") {
      get {
        onSuccess(servise.getProjects()) { projects =>
          complete(projects.asJson)
        }
      }
      /*pathEnd {
        get {
          onSuccess(servise.getProjects()) { projects =>
            complete(projects.asJson)
          }
        } ~ post {
          entity(as[CreateProject]) { create =>
            onSuccess(servise.createProject(create)) { project =>
              complete(project.asJson)
            }
          }
        }
      }*//* ~ {
        path(Remaining) { id =>
          pathEnd {
            delete {
              //Delete
              complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
            } ~ put {
              //Delete
              complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
            }
          }
        }
      }*/
    }
}
