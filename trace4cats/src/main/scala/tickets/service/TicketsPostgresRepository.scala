package tickets.service

import cats.data.NonEmptyList
import cats.implicits._
import cats.effect.{Async, Sync}
import doobie._
import doobie.implicits._
import tickets.PostgreConfiguration
import tickets.model.Ticket

class TicketsPostgresRepository[F[_]: Sync: Async](configuration: PostgreConfiguration) {

  private val xa = Transactor.fromDriverManager[F](
    "org.postgresql.Driver",
    configuration.url,
    "tickets_user",
    "tickets_password"
  )


  def getByIds(ids: NonEmptyList[Long]): F[List[Ticket]] = {
    (fr"select * from tickets where" ++ Fragments.in(fr"id", ids))
      .query[Ticket]
      .stream
      .transact(xa)
      .compile
      .toList
  }

  def getById(id: Long): F[Option[Ticket]] = {
    sql"select * from tickets where id = $id"
      .query[Ticket]
      .stream
      .transact(xa)
      .compile
      .toList
      .map(_.headOption)
  }

  def getAll: F[List[Ticket]] = {
    sql"select * from tickets"
      .query[Ticket]
      .stream
      .transact(xa)
      .compile
      .toList
  }

  def create(ticket: Ticket): F[Ticket] = {
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

  def update(ticket: Ticket): F[Int] = {
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

  def delete(id: Long): F[Int] = {
    sql"delete from tickets where id = $id"
      .update
      .run
      .transact(xa)
  }
}
