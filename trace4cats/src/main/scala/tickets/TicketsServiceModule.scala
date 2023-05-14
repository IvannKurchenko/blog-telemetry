package tickets

import cats.effect.{Async, Concurrent, LiftIO, Resource, Sync}
import cats.implicits._
import io.opentelemetry.api.GlobalOpenTelemetry
import org.typelevel.otel4s.Otel4s
import org.typelevel.otel4s.java.OtelJava
import org.typelevel.otel4s.metrics.UpDownCounter
import org.typelevel.otel4s.trace.Tracer
import tickets.http.TicketsServiceApi
import tickets.service._

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

class TicketsServiceModule[F[_]: Sync: Async: Concurrent: Tracer](
                                                                   configuration: TicketsConfiguration,
                                                          ticketsCounter: UpDownCounter[F, Long]) {
  implicit lazy val executionContext: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))

  lazy val kafka = new TicketsKafkaProducer[F](configuration.kafka)
  lazy val repo = new TicketsPostgresRepository[F](configuration.postgre)
  lazy val elastic = new TicketsElasticRepository[F](configuration.elasticsearch)
  lazy val projects = new ProjectsServiceClient[F](configuration.projects)
  lazy val service = new TicketsService[F](repo, elastic, kafka, projects, ticketsCounter)
  lazy val api = new TicketsServiceApi[F](service)
}

object TicketsServiceModule {

  def build[F[_]: Sync: Async: Concurrent: LiftIO] = {
    otelResource.use(buildInternal[F])
  }

  private def otelResource[F[_]: Sync: Async: LiftIO] = {
    Resource
      .eval(Sync[F].delay(GlobalOpenTelemetry.get))
      .evalMap(OtelJava.forAsync[F])
  }

  private def buildInternal[F[_]: Sync: Async: Concurrent: LiftIO](otel: Otel4s[F]) = {
    for {
      configuration <- Sync[F].delay(TicketsConfiguration.load)
      telemetry <- Sync[F].delay(GlobalOpenTelemetry.get)
      otel <- OtelJava.forAsync[F](telemetry)

      traceProvider <- otel.tracerProvider.get("tickets-service")
      metricsProvider <- otel.meterProvider.get("tickets-service")

      ticketsCounter <- metricsProvider
        .upDownCounter("tickets_count")
        .withUnit("1")
        .withDescription("Tickets count")
        .create

    } yield {
      implicit val tracer: Tracer[F] = traceProvider
      new TicketsServiceModule[F](configuration, ticketsCounter)
    }
  }
}
