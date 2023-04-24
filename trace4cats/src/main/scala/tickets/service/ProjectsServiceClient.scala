package tickets.service

import tickets.ProjectsServiceConfiguration
import tickets.model.Project

import cats.effect._
import org.http4s.implicits._
import org.http4s._
import org.http4s.client._
import org.http4s.circe._
import org.http4s.circe.CirceEntityCodec._
import org.http4s.dsl.io._
import org.http4s.ember.client.EmberClientBuilder
import io.circe.generic.auto._

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
