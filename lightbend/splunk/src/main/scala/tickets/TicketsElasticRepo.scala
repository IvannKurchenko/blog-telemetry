package tickets

import com.sksamuel.elastic4s.{ElasticClient, ElasticProperties, Response}
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.fields.TextField
import com.sksamuel.elastic4s.http.JavaClient
import io.circe.generic.auto._
import com.sksamuel.elastic4s.circe._
import com.sksamuel.elastic4s.requests.common.RefreshPolicy
import com.sksamuel.elastic4s.requests.indexes.{CreateIndexResponse, IndexResponse}

import scala.concurrent.{ExecutionContext, Future}

class TicketsElasticRepo(configuration: ElasticsearchConfiguration)
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

  def searchTickets(query: String): Future[List[SearchTicket]] = {
    client.execute {
      search("tickets").query(query)
    }.map(_.result.to[SearchTicket].toList)
  }
}
