package tickets

import scala.concurrent.{ExecutionContext, Future}

class TicketsService(repo: TicketsRepo,
                     elastic: TicketsElasticRepo,
                     kafka: TicketsKafkaProducer,
                     projects: ProjectsServiceClient)
                    (implicit ec: ExecutionContext) {


  def createTicket(create: CreateTicket): Future[Ticket] = {
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
      _ <- verifyProject(ticket.project)
      ticket <- repo.create(ticket)
      _ <- elastic.indexTicket(ticket)
      _ <- kafka.sendCreated(ticket)
    } yield ticket
  }

  def searchTickets(query: Option[String], project: Option[Long]): Future[List[Ticket]] = {
    for {
      foundTickets <- elastic.searchTickets(query, project)
      tickets <- repo.getByIds(foundTickets.map(_.id).toSet)
    } yield tickets
  }

  def update(update: UpdateTicket): Future[Option[Ticket]] = {
    for {
      _ <- verifyProject(update.project)
      existing <- repo.getById(update.id)
      updated = existing.map(
        _.copy(
          project = update.project,
          title = update.title,
          description = update.description,
          modifiedBy = update.updater,
          modifiedAt = System.currentTimeMillis()
        )
      )
      _ <- updated.fold(Future.successful()) { ticket =>
        for {
          _ <- repo.update(ticket)
          _ <- elastic.indexTicket(ticket)
          _ <- kafka.sendUpdated(ticket)
        } yield ()
      }
    } yield updated
  }

  def delete(id: Long): Future[Boolean] = {
    for {
      count <- repo.delete(id)
      _ <- elastic.deleteTicket(id)
    } yield count > 0
  }

  private def verifyProject(project: Long): Future[Unit] = {
    for {
      project <- projects.findProject(project)
      _ <- project.fold(Future.failed[Unit](new Exception(s"No project found: $project"))) { _ =>
        Future.successful(())
      }
    } yield ()
  }
}
