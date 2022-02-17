package tickets.service

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.circe._
import com.sksamuel.elastic4s.fields.TextField
import com.sksamuel.elastic4s.http.JavaClient
import com.sksamuel.elastic4s.requests.common.RefreshPolicy
import com.sksamuel.elastic4s.requests.indexes.CreateIndexResponse
import com.sksamuel.elastic4s.requests.searches.queries.Query
import com.sksamuel.elastic4s.{ElasticClient, ElasticProperties, Response}
import io.circe.generic.auto._
import tickets.ElasticsearchConfiguration
import tickets.model.{SearchTicket, Ticket}

import scala.concurrent.{ExecutionContext, Future}

class TicketsElasticRepository(configuration: ElasticsearchConfiguration)
                              (implicit ec: ExecutionContext) {

  private val client = {
    ElasticClient(JavaClient(ElasticProperties(configuration.url)))
  }

  def init: Future[Response[CreateIndexResponse]] = {
    client.execute {
      createIndex("tickets").mapping(
        properties(
          TextField("title"),
          TextField("description")
        )
      )
    }
  }

  def indexTicket(ticket: Ticket): Future[Unit] = {
    client.execute {
      indexInto("tickets").
        id(ticket.id.toString).
        source(ticket.toSearch).
        refresh(RefreshPolicy.Immediate)
    }.map(_ => ())
  }

  def searchTickets(searchQuery: Option[String], projectId: Option[Long]): Future[List[SearchTicket]] = {
    client.execute {
      val queries: List[Query] = searchQuery.toList.map(query) ++ projectId.toList.map(termQuery("project", _))
      search("tickets").bool(must(queries))
    }.map(_.result.to[SearchTicket].toList)
  }

  def deleteTicket(id: Long): Future[Unit] = {
    client.execute {
      deleteById("tickets", id.toString).refresh(RefreshPolicy.Immediate)
    }.map(_ => ())
  }
}
