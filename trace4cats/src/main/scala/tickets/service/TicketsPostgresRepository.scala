package tickets.service

import cats.data.NonEmptyList
import cats.implicits._
import cats.effect.{Async, Sync}
import doobie._
import doobie.implicits._
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.trace.Tracer
import tickets.PostgreConfiguration
import tickets.model.Ticket

class TicketsPostgresRepository[F[_]: Sync: Async: Tracer](configuration: PostgreConfiguration) {

  private val xa = Transactor.fromDriverManager[F](
    "org.postgresql.Driver",
    configuration.url,
    "tickets_user",
    "tickets_password"
  )


  def getByIds(ids: NonEmptyList[Long]): F[List[Ticket]] = {
    Tracer[F]
      .span("postgres.getByIds", Attribute("ids", ids.toList.mkString(",")))
      .surround {
        (fr"select * from tickets where" ++ Fragments.in(fr"id", ids))
          .query[Ticket]
          .stream
          .transact(xa)
          .compile
          .toList
      }
  }

  def getById(id: Long): F[Option[Ticket]] = {
    Tracer[F]
      .span("postgres.getByIds", Attribute("id", id.toString))
      .surround {
        sql"select * from tickets where id = $id"
          .query[Ticket]
          .stream
          .transact(xa)
          .compile
          .toList
          .map(_.headOption)
      }
  }

  def create(ticket: Ticket): F[Ticket] = {
    Tracer[F]
      .span("create")
      .surround {
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
  }

  def update(ticket: Ticket): F[Int] = {
    Tracer[F]
      .span("postgres.update", Attribute("id", ticket.id.toString))
      .surround {
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
  }

  def delete(id: Long): F[Int] = {
    Tracer[F]
      .span("postgres.delete", Attribute("id", id.toString))
      .surround {
        sql"delete from tickets where id = $id"
          .update
          .run
          .transact(xa)
      }
  }
}
