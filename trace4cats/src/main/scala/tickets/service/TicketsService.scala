package tickets.service

import cats.data.NonEmptyList
import cats.effect.IO
import org.typelevel.log4cats.{Logger, SelfAwareStructuredLogger}
import org.typelevel.log4cats.slf4j.Slf4jLogger
import tickets.model.{CreateTicket, Ticket, UpdateTicket}

class TicketsService(postgres: TicketsPostgresRepository,
                     elastic: TicketsElasticRepository,
                     kafka: TicketsKafkaProducer,
                     projects: ProjectsServiceClient,
                     /*ticketsCounter: LongCounter*/) {
  implicit def logger: Logger[IO] = Slf4jLogger.getLogger[IO]

  def createTicket(create: CreateTicket): IO[Ticket] = {
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

  def searchTickets(query: Option[String], project: Option[Long]): IO[List[Ticket]] = {
    for {
      foundTickets <- elastic.searchTickets(query, project)
      _ <- logger.info(s"Found tickets: $foundTickets")
      tickets <- NonEmptyList.fromList(foundTickets.toList).fold(IO.pure(List.empty[Ticket])) { tickets =>
        postgres.getByIds(tickets.map(_.id))
      }
    } yield tickets
  }

  def update(update: UpdateTicket): IO[Option[Ticket]] = {
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
      _ <- updated.fold(IO.unit) { ticket =>
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

  def delete(id: Long): IO[Boolean] = {
    for {
      count <- postgres.delete(id)
      _ <- elastic.deleteTicket(id)
      //_ <- Future(ticketsCounter.add(-1))
    } yield count > 0
  }

  private def verifyProject(project: Long): IO[Unit] = {
    for {
      project <- projects.findProject(project)
      _ <- project.fold(IO.raiseError[Unit](new Exception(s"No project found: $project")))(_ => IO.unit)
    } yield ()
  }
}
