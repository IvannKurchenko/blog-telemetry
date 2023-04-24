package tickets

import pureconfig._
import pureconfig.generic.auto._

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext
import tickets.service._

class TicketsServiceModule {
  implicit lazy val executionContext: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))

  lazy val configuration: TicketsConfiguration = ConfigSource.default.loadOrThrow[TicketsConfiguration]

  lazy val kafka = new TicketsKafkaProducer(configuration.kafka)
  lazy val repo = new TicketsPostgresRepository()
  lazy val elastic = new TicketsElasticRepository(configuration.elasticsearch)
  lazy val projects = new ProjectsServiceClient(configuration.projects)
  lazy val service = new TicketsService(repo, elastic, kafka, projects)
  lazy val api = new TicketsServiceApi(service)
}
