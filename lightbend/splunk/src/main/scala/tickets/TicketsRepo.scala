package tickets

import slick.jdbc.PostgresProfile.api._
import slick.migration.api._
import tickets.TicketsRepo.TicketsTable

import scala.concurrent.{ExecutionContext, Future}

class TicketsRepo(implicit ec: ExecutionContext) {

  private implicit val postgresDialect: PostgresDialect = new PostgresDialect
  private val db = Database.forConfig("postgre")
  private val ticketsTable = TableQuery[TicketsTable]

  def init: Future[Unit] = {
    val init =
      TableMigration(ticketsTable)
        .create
        .addColumns(_.id, _.project, _.title, _.description, _.createdAt, _.createdBy, _.modifiedAt, _.modifiedBy)

    val seed =
      SqlMigration("Create tickets table")

    val migration = init & seed
    db.run(migration())
  }

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

object TicketsRepo {
  class TicketsTable(tag: Tag) extends Table[Ticket](tag, "tickets") {
    val id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    val project = column[Long]("project")
    val title = column[String]("title")
    val description = column[String]("description")
    val createdAt = column[Long]("created_at")
    val createdBy = column[String]("created_by")
    val modifiedAt = column[Long]("modified_at")
    val modifiedBy = column[String]("modified_by")
    def * =
      (id, project, title, description, createdAt, createdBy, modifiedAt, modifiedBy).mapTo[Ticket]
  }
}
