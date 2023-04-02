package tickets

import akka.Done
import akka.actor.CoordinatedShutdown
import akka.http.scaladsl.Http
import com.typesafe.scalalogging.LazyLogging
import kamon.Kamon
import org.flywaydb.core.Flyway

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Failure, Success}

object TicketsServiceApplication extends LazyLogging {
  def main(args: Array[String]): Unit = {
    Kamon.init()

    val module = new TicketsServiceModule()
    import module._

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
