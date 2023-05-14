package tickets.telemetry

import cats.effect.Async
import com.fasterxml.jackson.module.scala.JavaTypeable
import com.sksamuel.elastic4s._
import org.typelevel.otel4s.trace.Tracer

import scala.concurrent.Future

trait ElasticMiddleware {
  implicit class ElasticMiddlewareOps[F[_]: Tracer: Async, A](client: ElasticClient) {
    def executeTraced[T, U](t: T)(implicit
                                  handler: Handler[T, U],
                                  javaTypeable: JavaTypeable[U],
                                  options: CommonRequestOptions): F[Response[U]] = {
      Tracer[F].span("elasticsearch").surround(
        Async[F].fromFuture(Async[F].pure(client.execute[T, U, Future](t)))
      )
    }
  }
}

object ElasticMiddleware extends ElasticMiddleware