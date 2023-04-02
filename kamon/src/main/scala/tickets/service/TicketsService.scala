package tickets.service

import com.typesafe.scalalogging.LazyLogging
import kamon.Kamon
import kamon.metric.Metric
import tickets.model.{CreateTicket, Ticket, UpdateTicket}

import scala.concurrent.{ExecutionContext, Future}

class TicketsService(postgre: TicketsPostgreRepository,
                     elastic: TicketsElasticRepository,
                     kafka: TicketsKafkaProducer,
                     projects: ProjectsServiceClient,
                     ticketsGauge: Metric.Gauge)
                    (implicit ec: ExecutionContext) extends LazyLogging{


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

    logger.info(s"Creating ticket: $ticket")
    for {
      _ <- verifyProject(ticket.project)
      ticket <- postgre.create(ticket)
      _ <- elastic.indexTicket(ticket)
      _ <- kafka.sendCreated(ticket)
      _ <- Future(ticketsGauge.withTag("project_id", ticket.project).increment())
    } yield {
      logger.info(s"Created ticket: $ticket")
      ticket
    }
  }

  def searchTickets(query: Option[String], project: Option[Long]): Future[List[Ticket]] = {
    for {
      foundTickets <- elastic.searchTickets(query, project)
      tickets <- postgre.getByIds(foundTickets.map(_.id).toSet)
    } yield tickets
  }

  def update(update: UpdateTicket): Future[Option[Ticket]] = {
    logger.info(s"Updating ticket: $update")
    for {
      _ <- verifyProject(update.project)
      existing <- postgre.getById(update.id)
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
          _ <- postgre.update(ticket)
          _ <- elastic.indexTicket(ticket)
          _ <- kafka.sendUpdated(ticket)
        } yield ()
      }
    } yield {
      logger.info(s"Updated ticket: $updated")
      updated
    }
  }

  def delete(id: Long): Future[Boolean] = {
    for {
      count <- postgre.delete(id)
      _ <- elastic.deleteTicket(id)
      _ <- Future(ticketsGauge.withoutTags().decrement())
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
