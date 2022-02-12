package tickets

import akka.Done
import akka.actor.CoordinatedShutdown
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.stream.Materializer
import com.typesafe.scalalogging.LazyLogging
import org.mongodb.scala._
import pureconfig._
import pureconfig.generic.auto._

import scala.concurrent.duration._

object ProjectsServiceApplication extends LazyLogging {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem(Behaviors.empty, "application-system")
    implicit val executionContext = system.executionContext
    implicit val materializer = Materializer(system)

    val configuration = ConfigSource.default
      .at("application")
      .loadOrThrow[ProjectApplicationConfiguration]

    val client: MongoClient = MongoClient(configuration.mongoUrl)
    val database: MongoDatabase = client.getDatabase("projects")

    val projectsService = new ProjectsService(database)
    val projectsApi = new ProjectsApi(projectsService)

    val binding = Http()
      .newServerAt(configuration.host, configuration.port)
      .bind(projectsApi.route)


    val shutdown = CoordinatedShutdown(system)
    shutdown.addTask(CoordinatedShutdown.PhaseServiceUnbind, "http-unbind") { () =>
      binding.flatMap(_.unbind()).map(_ => Done)
    }

    shutdown.addTask(CoordinatedShutdown.PhaseServiceRequestsDone, "http-unbind") { () =>
      binding.flatMap(_.terminate(10.seconds)).map(_ => Done)
    }
  }
}
