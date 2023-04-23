package tickets.service

import cats.effect.IO
import tickets.model.{CreateTicket, Ticket, UpdateTicket}

class TicketsService(postgres: TicketsPostgresRepository,
                     elastic: TicketsElasticRepository,
                     kafka: TicketsKafkaProducer,
                     projects: ProjectsServiceClient,
                     /*ticketsCounter: LongCounter*/) {

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

    //logger.info(s"Creating ticket: $ticket")
    for {
      _ <- verifyProject(ticket.project)
      ticket <- postgres.create(ticket)
      _ <- elastic.indexTicket(ticket)
      _ <- kafka.sendCreated(ticket)
      //_ <- Future(ticketsCounter.add(1))
    } yield {
      //logger.info(s"Created ticket: $ticket")
      ticket
    }
  }

  def searchTickets(query: Option[String], project: Option[Long]): IO[List[Ticket]] = {
    for {
      foundTickets <- elastic.searchTickets(query, project)
      tickets <- postgres.getByIds(foundTickets.map(_.id).toSet)
    } yield tickets
  }

  def update(update: UpdateTicket): IO[Option[Ticket]] = {
    //logger.info(s"Updating ticket: $update")
    for {
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
    } yield {
      //logger.info(s"Updated ticket: $updated")
      updated
    }
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
