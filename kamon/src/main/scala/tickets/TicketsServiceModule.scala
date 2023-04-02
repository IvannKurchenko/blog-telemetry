package tickets

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.Materializer
import kamon.Kamon
import kamon.instrumentation.executor.ExecutorInstrumentation
import kamon.metric.Metric._
import pureconfig._
import pureconfig.generic.auto._
import tickets.service._

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

class TicketsServiceModule {
  implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "application-system")
  implicit val executionContext: ExecutionContext = {
    val pool = Executors.newFixedThreadPool(2)
    ExecutionContext.fromExecutor(ExecutorInstrumentation.instrument(pool, "main-executor"))
  }

  implicit val materializer: Materializer = Materializer(system)

  lazy val configuration: TicketsConfiguration = ConfigSource.default.loadOrThrow[TicketsConfiguration]

  lazy val ticketsGauge: Gauge = Kamon.gauge("tickets_count", "Total number of tickets")

  lazy val kafka = new TicketsKafkaProducer(configuration.kafka)
  lazy val repo = new TicketsPostgreRepository()
  lazy val elastic = new TicketsElasticRepository(configuration.elasticsearch)
  lazy val projects = new ProjectsServiceClient(configuration.projects)
  lazy val service = new TicketsService(repo, elastic, kafka, projects, ticketsGauge)
  lazy val api = new TicketsServiceApi(service)
}
