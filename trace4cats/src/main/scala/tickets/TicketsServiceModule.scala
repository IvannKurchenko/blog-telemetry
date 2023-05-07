package tickets

import cats.effect.{Async, Concurrent, Sync}
import org.typelevel.otel4s.java.metrics.Metrics
import org.typelevel.otel4s.metrics.{Meter, UpDownCounter}
import org.typelevel.otel4s.trace.Tracer
import tickets.service._

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

class TicketsServiceModule[F[_]: Sync: Async: Concurrent](configuration: TicketsConfiguration,
                                                          ticketsCounter: UpDownCounter[F, Long])
                                                         (implicit tracer: Tracer[F]) {
  implicit lazy val executionContext: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))

  lazy val kafka = new TicketsKafkaProducer[F](configuration.kafka)
  lazy val repo = new TicketsPostgresRepository[F](configuration.postgre)
  lazy val elastic = new TicketsElasticRepository[F](configuration.elasticsearch)
  lazy val projects = new ProjectsServiceClient[F](configuration.projects)
  lazy val service = new TicketsService[F](repo, elastic, kafka, projects, ticketsCounter)
  lazy val api = new TicketsServiceApi[F](service)
}
