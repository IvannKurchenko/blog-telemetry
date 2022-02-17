package tickets.service

import slick.jdbc.PostgresProfile.api._
import tickets.model.Ticket
import tickets.service.TicketsPostgreRepository.TicketsTable

import scala.concurrent.{ExecutionContext, Future}

class TicketsPostgreRepository(implicit ec: ExecutionContext) {

  private val db = Database.forConfig("postgre")
  private val ticketsTable = TableQuery[TicketsTable]

  def getByIds(ids: Set[Long]): Future[List[Ticket]] = {
    db.run(ticketsTable.filter(_.id inSet ids).result.map(_.toList))
  }

  def getById(id: Long): Future[Option[Ticket]] = {
    db.run(ticketsTable.filter(_.id === id).result.headOption)
  }

  def getAll: Future[List[Ticket]] = {
    db.run(ticketsTable.result.map(_.toList))
  }

  def create(ticket: Ticket): Future[Ticket] = {
    db.run(ticketsTable returning ticketsTable.map(_.id) into ((ticket, id) => ticket.copy(id=id)) += ticket)
  }

  def update(ticket: Ticket): Future[Int] = {
    db.run(ticketsTable.update(ticket))
  }

  def delete(id: Long): Future[Int] = {
    db.run(ticketsTable.filter(_.id === id).delete)
  }
}

object TicketsPostgreRepository {
  class TicketsTable(tag: Tag) extends Table[Ticket](tag, "tickets") {
    val id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    val project = column[Long]("project")
    val title = column[String]("title")
    val description = column[String]("description")
    val createdAt = column[Long]("created_at")
    val createdBy = column[String]("created_by")
    val modifiedAt = column[Long]("modified_at")
    val modifiedBy = column[String]("modified_by")
    def * = {
      (id, project, title, description, createdAt, createdBy, modifiedAt, modifiedBy).mapTo[Ticket]
    }
  }
}
