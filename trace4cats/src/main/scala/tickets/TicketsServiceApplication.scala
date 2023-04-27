package tickets

import cats.effect.{ExitCode, IO, IOApp}
import com.comcast.ip4s.{Host, Port}
import org.flywaydb.core.Flyway
import org.http4s.ember.server.EmberServerBuilder
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger


object TicketsServiceApplication extends IOApp {
  implicit def logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  def run(args: List[String]): IO[ExitCode] = {
    val module = new TicketsServiceModule[IO]()
    import module._

    for {
      _ <- logger.info("Initializing application")

      migrateResult <- IO.delay {
        Flyway.configure.dataSource(configuration.postgre.url, null, null).load.migrate
      }
      _ <- logger.info(s"Repository initialized. Result: $migrateResult")

      elasticInitResult <- elastic.init
      _ <- logger.info(s"Elastic initialized: $elasticInitResult")

      host <- IO.defer {
        Host
          .fromString(configuration.application.host)
          .fold(IO.raiseError[Host](new IllegalArgumentException("Invalid host")))(IO.pure)
      }


      port <- IO.defer {
        Port
          .fromInt(configuration.application.port)
          .fold(IO.raiseError[Port](new IllegalArgumentException("Invalid port")))(IO.pure)
      }

      code <- EmberServerBuilder
        .default[IO]
        .withHost(host)
        .withPort(port)
        .withHttpApp(api.app)
        .build
        .use(_ => IO.never)
        .as(ExitCode.Success)

    } yield code
  }
}
