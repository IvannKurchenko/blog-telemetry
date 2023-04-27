package tickets.service

import cats.effect._
import cats.implicits._
import fs2.io.net.Network
import io.circe.generic.auto._
import org.http4s._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.ember.client.EmberClientBuilder
import tickets.ProjectsServiceConfiguration
import tickets.model.Project

class ProjectsServiceClient[F[_]](configuration: ProjectsServiceConfiguration)
                                                       (implicit F: Sync[F], A: Async[F], N: Network[F]) {

  def findProject(id: Long): F[Option[Project]] = {
    EmberClientBuilder
      .default[F]
      .build
      .use { client =>
        val request = Request[F](Method.GET, Uri.unsafeFromString(s"${configuration.url}/projects/$id"))
        client.run(request).use { response =>
          response.status match {
            case Status.NotFound => F.pure(None)
            case Status.Ok => response.as[Project].map(Some(_))
            case status =>
              response.as[String].flatMap { body =>
                F.raiseError(new Exception(s"Failure response code received $status: $body"))
              }
          }
        }
      }
  }
}
