package tickets.service

import cats.effect.IO
import com.fasterxml.jackson.module.scala.JavaTypeable
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.circe._
import com.sksamuel.elastic4s.fields.TextField
import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.requests.common.RefreshPolicy
import com.sksamuel.elastic4s.requests.indexes.CreateIndexResponse
import com.sksamuel.elastic4s.requests.searches.queries.Query
import com.sksamuel.elastic4s._
import io.circe.generic.auto._
import tickets.ElasticsearchConfiguration
import tickets.model.{SearchTicket, Ticket}

import scala.concurrent.{ExecutionContext, Future}


class TicketsElasticRepository(configuration: ElasticsearchConfiguration)(implicit ec: ExecutionContext) {

  private val client: ElasticClient = {
    ElasticClient(JavaClient(ElasticProperties(configuration.url)))
  }

  def init: IO[Response[CreateIndexResponse]] = {
    execute(createIndex("tickets").mapping(properties(TextField("title"), TextField("description"))))
  }

  def indexTicket(ticket: Ticket): IO[Unit] = {
    execute {
      indexInto("tickets").
        id(ticket.id.toString).
        source(ticket.toSearch).
        refresh(RefreshPolicy.Immediate)
    }.as(())
  }

  def searchTickets(searchQuery: Option[String], projectId: Option[Long]): IO[List[SearchTicket]] = {
    val queries: List[Query] = searchQuery.toList.map(query) ++ projectId.toList.map(termQuery("project", _))
    execute(search("tickets").bool(must(queries))).map(_.result.to[SearchTicket].toList)
  }

  def deleteTicket(id: Long): IO[Unit] = {
    execute(deleteById("tickets", id.toString).refresh(RefreshPolicy.Immediate)).as(())
  }


  /**
   * 'Elastic4S Cats Effect' is not yet available for Scala 3:
   * https://mvnrepository.com/artifact/com.sksamuel.elastic4s/elastic4s-cats-effect
   */
  private def execute[T, U, F[_]](t: T)(implicit
                                        executor: Executor[F],
                                        functor: Functor[F],
                                        handler: Handler[T, U],
                                        javaTypeable: JavaTypeable[U],
                                        options: CommonRequestOptions): IO[Response[U]] = {
    IO.fromFuture(IO.pure(client.execute[T, U, Future](t)))
  }
}
