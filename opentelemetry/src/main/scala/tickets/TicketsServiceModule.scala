package tickets

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.stream.Materializer
import io.opentelemetry.api.metrics.{LongCounter, Meter}
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk
import pureconfig._
import pureconfig.generic.auto._
import tickets.service._

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

class TicketsServiceModule {
  implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "application-system")
  implicit val executionContext: ExecutionContext = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))
  implicit val materializer: Materializer = Materializer(system)

  lazy val configuration: TicketsConfiguration = ConfigSource.default.loadOrThrow[TicketsConfiguration]

  /*
   * `AutoConfiguredOpenTelemetrySdk` instantiate client based on provided environment variables or Java options
   */
  lazy val sdk = AutoConfiguredOpenTelemetrySdk.initialize().getOpenTelemetrySdk
  lazy val ticketsMeter: Meter = sdk.meterBuilder("tickets-service").build()
  lazy val ticketsCounter: LongCounter = ticketsMeter
    .counterBuilder("tickets_count")
    .setDescription("Total number of tickets")
    .setUnit("1")
    .build()

  lazy val kafka = new TicketsKafkaProducer(configuration.kafka)
  lazy val repo = new TicketsPostgreRepository()
  lazy val elastic = new TicketsElasticRepository(configuration.elasticsearch)
  lazy val projects = new ProjectsServiceClient(configuration.projects)
  lazy val service = new TicketsService(repo, elastic, kafka, projects, ticketsCounter)
  lazy val api = new TicketsServiceApi(service)
}
