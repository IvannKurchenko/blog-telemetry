package tickets.telemetry

import cats.effect.Async
import cats.implicits._
import org.http4s.client.Client
import org.http4s.{Request, Response}
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.trace.{Span, SpanKind, Status, Tracer}

trait ClientMiddleware {
  implicit class ClientMiddlewareOps[F[_]: Tracer: Async, A](client: Client[F]) {
    def runTraced(request: Request[F]): F[Response[F]] = {
      Tracer[F].spanBuilder("client-request")
        .addAttribute(Attribute("http.method", request.method.name))
        .addAttribute(Attribute("http.url", request.uri.renderString))
        .withSpanKind(SpanKind.Client)
        .wrapResource(client.run(request))
        .build
        .use {
          case span @ Span.Res(response) =>
            for {
              _ <- span.addAttribute(Attribute("http.status-code", response.status.code.toLong))
              _ <- if (response.status.isSuccess) span.setStatus(Status.Ok) else span.setStatus(Status.Error)
            } yield response
        }
    }
  }
}

object ClientMiddleware extends ClientMiddleware
