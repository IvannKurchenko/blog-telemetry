package tickets

import cats.effect.{ExitCode, IO, IOApp}
import org.flywaydb.core.Flyway
import org.http4s.blaze.server.BlazeServerBuilder
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

object TicketsServiceApplication extends IOApp {
  implicit def logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  def run(args: List[String]): IO[ExitCode] = {
    for {
      configuration <- IO.delay(TicketsConfiguration.load)

      _ <- logger.info("Initializing application")
      module <- TicketsServiceModule.build[IO]

      migrateResult <- IO.delay {
        Flyway.configure.dataSource(configuration.postgre.url, null, null).load.migrate
      }

      _ <- logger.info(s"Repository initialized. Result: $migrateResult")

      elasticInitResult <- module.elastic.init
      _ <- logger.info(s"Elastic initialized: $elasticInitResult")

      code <- BlazeServerBuilder[IO]
        .bindHttp(configuration.application.port, configuration.application.host)
        .withHttpApp(module.api.app)
        .serve
        .compile
        .drain
        .as(ExitCode.Success)

    } yield code
  }
}
