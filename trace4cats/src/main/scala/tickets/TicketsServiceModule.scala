package tickets

import cats.effect.{Async, Concurrent, IO, Sync}
import pureconfig._
import pureconfig.generic.auto._

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import tickets.service._

class TicketsServiceModule[F[_]: Sync: Async: Concurrent] {
  implicit lazy val executionContext: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))

  lazy val configuration: TicketsConfiguration = ConfigSource.default.loadOrThrow[TicketsConfiguration]

  lazy val kafka = new TicketsKafkaProducer[F](configuration.kafka)
  lazy val repo = new TicketsPostgresRepository[F]()
  lazy val elastic = new TicketsElasticRepository[F](configuration.elasticsearch)
  lazy val projects = new ProjectsServiceClient[F](configuration.projects)
  lazy val service = new TicketsService[F](repo, elastic, kafka, projects)
  lazy val api = new TicketsServiceApi[F](service)
}
