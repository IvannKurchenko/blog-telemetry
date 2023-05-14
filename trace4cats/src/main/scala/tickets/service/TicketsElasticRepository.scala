package tickets.service

import cats.effect.{Async, Sync}
import cats.implicits._
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s._
import com.sksamuel.elastic4s.circe._
import com.sksamuel.elastic4s.fields.TextField
import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.requests.common.RefreshPolicy
import com.sksamuel.elastic4s.requests.indexes.CreateIndexResponse
import com.sksamuel.elastic4s.requests.searches.queries.Query
import io.circe.generic.auto._
import org.typelevel.otel4s.trace.Tracer
import tickets.ElasticsearchConfiguration
import tickets.model.{SearchTicket, Ticket}
import tickets.telemetry._

import scala.concurrent.ExecutionContext


class TicketsElasticRepository[F[_]: Async: Sync: Tracer](configuration: ElasticsearchConfiguration)
                                                         (implicit ec: ExecutionContext) {

  private val client: ElasticClient = {
    ElasticClient(JavaClient(ElasticProperties(configuration.url)))
  }

  def init: F[Response[CreateIndexResponse]] = {
    client.executeTraced(
      createIndex("tickets").mapping(properties(TextField("title"), TextField("description")))
    )
  }

  def indexTicket(ticket: Ticket): F[Unit] = {
    client.executeTraced {
      indexInto("tickets").
        id(ticket.id.toString).
        source(ticket.toSearch).
        refresh(RefreshPolicy.Immediate)
    }.as(())
  }

  def searchTickets(searchQuery: Option[String], projectId: Option[Long]): F[List[SearchTicket]] = {
    val queries: List[Query] = searchQuery.toList.map(query) ++ projectId.toList.map(termQuery("project", _))
    client.executeTraced(search("tickets").bool(must(queries))).map(_.result.to[SearchTicket].toList)
  }

  def deleteTicket(id: Long): F[Unit] = {
    client.executeTraced(deleteById("tickets", id.toString).refresh(RefreshPolicy.Immediate)).as(())
  }
}
