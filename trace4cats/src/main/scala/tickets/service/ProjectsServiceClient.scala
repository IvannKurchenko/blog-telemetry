package tickets.service

import tickets.ProjectsServiceConfiguration
import tickets.model.Project

import cats.effect.*
import org.http4s.implicits.*
import org.http4s.*
import org.http4s.client.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.io.*
import org.http4s.ember.client.EmberClientBuilder
import io.circe.generic.auto.*

import scala.concurrent.ExecutionContext.Implicits.global

class ProjectsServiceClient(configuration: ProjectsServiceConfiguration) {

  def findProject(id: Long): IO[Option[Project]] = {
    EmberClientBuilder
      .default[IO]
      .build
      .use { client =>
        val request = Request[IO](Method.GET, Uri.unsafeFromString(s"${configuration.url}/projects/$id"))
        client.run(request).use { response =>
          response.status match {
            case Status.NotFound => IO.pure(None)
            case Status.Ok => response.as[Project].map(Some(_))
            case status =>
              response.as[String].flatMap { body =>
                IO.raiseError(new Exception(s"Failure response code received $status: $body"))
              }
          }
        }
      }
  }
}
