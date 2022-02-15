package tickets

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport._
import io.circe.generic.auto._

import scala.concurrent.{ExecutionContext, Future}

class ProjectsServiceClient(configuration: ProjectsServiceConfiguration)
                           (implicit system: ActorSystem, ec: ExecutionContext) {

  def findProject(id: Long): Future[Option[Project]] = {
    Http().singleRequest(HttpRequest(uri = s"${configuration.url}/projects/$id")).flatMap { response =>
      response.status match {
        case StatusCodes.NotFound => Future.successful(None)
        case StatusCodes.OK => Unmarshal(response.entity).to[Project].map(Some(_))
        case status =>
          Unmarshal(response.entity).to[String].flatMap { body =>
            Future.failed(new Exception(s"Failure response code received $status: $body"))
          }
      }
    }
  }
}
