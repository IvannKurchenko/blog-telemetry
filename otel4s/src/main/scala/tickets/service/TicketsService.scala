package tickets.service

import cats.data.NonEmptyList
import cats.implicits._
import cats.effect.{Async, Sync}
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.{GlobalOpenTelemetry, OpenTelemetry}
import io.opentelemetry.context.ContextStorage
import org.http4s.dsl.Http4sDslBinCompat
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.java.metrics.Metrics
import org.typelevel.otel4s.metrics.{Meter, UpDownCounter}
import tickets.model.{CreateTicket, Ticket, UpdateTicket}

class TicketsService[F[_]](postgres: TicketsPostgresRepository[F],
                           elastic: TicketsElasticRepository[F],
                           kafka: TicketsKafkaProducer[F],
                           projects: ProjectsServiceClient[F],
                           ticketsCounter: UpDownCounter[F, Long])
                          (implicit F: Sync[F], A: Async[F]) {

  implicit def logger: Logger[F] = Slf4jLogger.getLogger[F]

  def createTicket(create: CreateTicket): F[Ticket] = {
    val ticket = Ticket(
      id = -1,
      project = create.project,
      title = create.title,
      description = create.description,
      createdAt = System.currentTimeMillis(),
      createdBy = create.creator,
      modifiedAt = System.currentTimeMillis(),
      modifiedBy = create.creator,
    )

    for {
      _ <- logger.info(s"Creating ticket: $ticket")

      _ <- logger.info(s"Checking projects exists: ${ticket.project}")
      _ <- verifyProject(ticket.project)

      _ <- logger.info(s"Persisting ticket in database: $ticket")
      ticket <- postgres.create(ticket)

      _ <- logger.info(s"Indexing ticket in Elasticsearch: $ticket")
      _ <- elastic.indexTicket(ticket)

      _ <- logger.info(s"Send ticket notification: $ticket")
      _ <- kafka.sendCreated(ticket)

      _ <- ticketsCounter.add(1, Attribute("project.id", ticket.id)) // Increase metrics counter
      _ <- logger.info(s"Created ticket: $ticket")
    } yield ticket
  }

  def searchTickets(query: Option[String], project: Option[Long]): F[List[Ticket]] = {
    for {
      foundTickets <- elastic.searchTickets(query, project)
      _ <- logger.info(s"Found tickets: $foundTickets")
      tickets <- NonEmptyList.fromList(foundTickets).fold(F.pure(List.empty[Ticket])) { tickets =>
        postgres.getByIds(tickets.map(_.id))
      }
    } yield tickets
  }

  def update(update: UpdateTicket): F[Option[Ticket]] = {
    for {
      _ <- logger.info(s"Updating ticket: $update")
      _ <- verifyProject(update.project)
      existing <- postgres.getById(update.id)
      updated = existing.map(
        _.copy(
          project = update.project,
          title = update.title,
          description = update.description,
          modifiedBy = update.updater,
          modifiedAt = System.currentTimeMillis()
        )
      )
      _ <- updated.fold(F.unit) { ticket =>
        for {
          _ <- postgres.update(ticket)
          _ <- elastic.indexTicket(ticket)
          _ <- kafka.sendUpdated(ticket)
        } yield ()
      }
      _ <- logger.info(s"Updated ticket: $updated")
    } yield
      updated
  }

  def delete(id: Long): F[Boolean] = {
    for {
      count <- postgres.delete(id)
      _ <- elastic.deleteTicket(id)
      _ <- ticketsCounter.add(-1)
    } yield count > 0
  }

  private def verifyProject(projectId: Long): F[Unit] = {
    for {
      project <- projects.findProject(projectId)
      _ <- project.fold(F.raiseError[Unit](new Exception(s"No project found: $project")))(_ => F.unit)
    } yield ()
  }
}
