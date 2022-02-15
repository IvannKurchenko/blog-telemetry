package tickets

import akka.Done
import akka.actor.CoordinatedShutdown
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.stream.Materializer
import com.typesafe.scalalogging.LazyLogging
import org.flywaydb.core.Flyway
import pureconfig._
import pureconfig.generic.auto._

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object TicketsServiceApplication extends LazyLogging {
  def main(args: Array[String]): Unit = {
    implicit val system = ActorSystem(Behaviors.empty, "application-system")
    implicit val executionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))
    implicit val materializer = Materializer(system)

    val configuration = ConfigSource.default.loadOrThrow[TicketsConfiguration]

    val kafka = new TicketsKafkaProducer(configuration.kafka)
    val repo = new TicketsRepo()
    val elastic = new TicketsElasticRepo(configuration.elasticsearch)
    val projects = new ProjectsServiceClient(configuration.projects)
    val service = new TicketsService(repo, elastic, kafka, projects)
    val api = new TicketsServiceApi(service)

    logger.info("Initializing application")

    val binding = for {
      _ <- Future {
        Flyway.configure.dataSource(configuration.postgre.url, null, null).load.migrate
      }
      _ = logger.info("Repository initialized")
      _ <- elastic.init
      _ = logger.info("Elastic initialized")
      binding <- {
        Http()
          .newServerAt(configuration.application.host, configuration.application.port)
          .bind(api.route)
      }
      _ = logger.info("Server bound")
    } yield binding


    val shutdown = CoordinatedShutdown(system)
    shutdown.addTask(CoordinatedShutdown.PhaseServiceUnbind, "http-unbind") { () =>
      binding.flatMap(_.unbind()).map(_ => Done)
    }

    shutdown.addTask(CoordinatedShutdown.PhaseServiceRequestsDone, "http-unbind") { () =>
      binding.flatMap(_.terminate(10.seconds)).map(_ => Done)
    }

    binding.onComplete {
      case Failure(exception) =>
        logger.error("Failed to start application", exception)
        System.exit(-1)
      case Success(_) => logger.info("Successfully started application")
    }
  }
}
