package tickets.service

import tickets.model.Ticket

import doobie._
import doobie.implicits._
import cats.effect.IO
import scala.concurrent.ExecutionContext

import cats.effect.unsafe.implicits.global

class TicketsPostgresRepository {

  private val xa = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    "jdbc:postgresql://localhost:5432/tickets",
    "tickets_user",
    "tickets_password"
  )


  def getByIds(ids: Set[Long]): IO[List[Ticket]] = {
    sql"select * from tickets where id in (${ids.mkString(",")})"
      .query[Ticket]
      .stream
      .transact(xa)
      .compile
      .toList
  }

  def getById(id: Long): IO[Option[Ticket]] = {
    sql"select * from tickets where id = id"
      .query[Ticket]
      .stream
      .transact(xa)
      .compile
      .toList
      .map(_.headOption)
  }

  def getAll: IO[List[Ticket]] = {
    sql"select * from tickets"
      .query[Ticket]
      .stream
      .transact(xa)
      .compile
      .toList
  }

  def create(ticket: Ticket): IO[Ticket] = {
    for {
      id <-
        sql"""insert into tickets (project, title, description, created_at, created_by, modified_at, modified_by) values
              (${ticket.project}, ${ticket.title}, ${ticket.description}, ${ticket.createdAt}, ${ticket.createdBy}, ${ticket.modifiedAt}, ${ticket.modifiedBy})"""
        .update
        .withUniqueGeneratedKeys[Long]("id")
        .transact(xa)

      createdTicket <- sql"select * from tickets where id = $id"
        .query[Ticket]
        .unique
        .transact(xa)
    } yield createdTicket
  }

  def update(ticket: Ticket): IO[Int] = {
    //db.run(ticketsTable.insertOrUpdate(ticket))
    sql"""update tickets set project = ${ticket.project},
          title = ${ticket.title},
          description = ${ticket.description},
          created_at = ${ticket.createdAt},
          created_by = ${ticket.createdBy},
          modified_at = ${ticket.modifiedAt},
          modified_by = ${ticket.modifiedBy}
          where id = ${ticket.id}"""
      .update
      .run
      .transact(xa)
  }

  def delete(id: Long): IO[Int] = {
    sql"delete from tickets where id = $id"
      .update
      .run
      .transact(xa)
  }
}
