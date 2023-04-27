package tickets.service

import cats.data.NonEmptyList
import cats.implicits._
import cats.effect.{Async, Sync}
import org.http4s.dsl.Http4sDslBinCompat
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import tickets.model.{CreateTicket, Ticket, UpdateTicket}

class TicketsService[F[_]](postgres: TicketsPostgresRepository[F],
                           elastic: TicketsElasticRepository[F],
                           kafka: TicketsKafkaProducer[F],
                           projects: ProjectsServiceClient[F],
                           /*ticketsCounter: LongCounter*/)(implicit F: Sync[F], A: Async[F]) {

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
      _ <- verifyProject(ticket.project)
      ticket <- postgres.create(ticket)
      _ <- elastic.indexTicket(ticket)
      _ <- kafka.sendCreated(ticket)
      //_ <- Future(ticketsCounter.add(1))
      _ <- logger.info(s"Created ticket: $ticket")
    } yield {
      ticket
    }
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
      //_ <- Future(ticketsCounter.add(-1))
    } yield count > 0
  }

  private def verifyProject(project: Long): F[Unit] = {
    for {
      project <- projects.findProject(project)
      _ <- project.fold(F.raiseError[Unit](new Exception(s"No project found: $project")))(_ => F.unit)
    } yield ()
  }
}
