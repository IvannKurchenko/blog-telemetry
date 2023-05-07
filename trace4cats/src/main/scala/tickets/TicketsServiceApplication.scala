package tickets

import cats.implicits._
import cats.syntax.all._
import cats.effect.kernel.Resource
import cats.effect.{ExitCode, IO, IOApp}
import io.opentelemetry.api.GlobalOpenTelemetry
import org.flywaydb.core.Flyway
import org.http4s.blaze.server.BlazeServerBuilder
import org.http4s.{Request, Response, Status}
import org.http4s.implicits._
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.otel4s.java.OtelJava
import org.typelevel.otel4s.trace.Tracer

import scala.util.control.NonFatal


object TicketsServiceApplication extends IOApp {
  implicit def logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  def run(args: List[String]): IO[ExitCode] = {

    for {
      configuration <- IO.delay(TicketsConfiguration.load)

      _ <- logger.info("Initializing application")

      module <- {

        Resource
          .eval(IO(GlobalOpenTelemetry.get))
          .evalMap(OtelJava.forAsync[IO])
          .evalMap { otel =>
            for {
              traceProvider <- otel.tracerProvider.get("tickets-service")
              metricsProvider <- otel.meterProvider.get("tickets-service")

              ticketsCounter <- metricsProvider
                .upDownCounter("tickets_count")
                .withUnit("1")
                .withDescription("Tickets count")
                .create

            } yield {
              implicit val tracer: Tracer[IO] = traceProvider
              new TicketsServiceModule[IO](configuration, ticketsCounter)
            }
          }
          .use(IO.pure)
      }

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
