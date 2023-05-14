package tickets.service

import cats.effect._
import cats.implicits._
import io.circe.generic.auto._
import org.http4s._
import org.http4s.blaze.client.BlazeClientBuilder
import org.http4s.circe.CirceEntityCodec._
import org.typelevel.otel4s.trace.Tracer
import tickets.ProjectsServiceConfiguration
import tickets.model.Project
import tickets.telemetry._

class ProjectsServiceClient[F[_] : Async : Sync : Tracer](configuration: ProjectsServiceConfiguration) {

  def findProject(projectId: Long): F[Option[Project]] = {
    BlazeClientBuilder[F].resource.use { client =>
      val request = Request[F](Method.GET, Uri.unsafeFromString(s"${configuration.url}/projects/$projectId"))
      client.runTraced(request).flatMap { response =>
        response.status match {
          case Status.NotFound => Sync[F].pure(none)
          case Status.Ok => response.as[Project].map(_.some)
          case status =>
            response.as[String].flatMap { body =>
              Sync[F].raiseError(new Exception(s"Failure response code received $status: $body"))
            }
        }
      }
    }
  }
}
