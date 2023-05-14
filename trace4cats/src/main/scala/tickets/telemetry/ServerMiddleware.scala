package tickets.telemetry

import cats.data.Kleisli
import cats.effect.{Async, Sync}
import cats.implicits._
import org.http4s.{Header, HttpApp, Request}
import org.typelevel.ci.CIString
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.trace.{SpanKind, Status, Tracer}

trait ServerMiddleware {
  implicit class ServerMiddlewareOps[F[_] : Sync : Async : Tracer](service: HttpApp[F]) {
    def traced: HttpApp[F] = {
      Kleisli { (req: Request[F]) =>
        Tracer[F]
          .spanBuilder("handle-request")
          .addAttribute(Attribute("http.method", req.method.name))
          .addAttribute(Attribute("http.url", req.uri.renderString))
          .withSpanKind(SpanKind.Server)
          .build
          .use { span =>

            for {
              response <- service(req)
              _ <- span.addAttribute(Attribute("http.status-code", response.status.code.toLong))
              _ <- {
                if (response.status.isSuccess) span.setStatus(Status.Ok) else span.setStatus(Status.Error)
              }
            } yield {
              // todo - https://www.w3.org/TR/trace-context/
              // add w3c headers
              val traceIdHeader = Header.Raw(CIString("traceId"), span.context.traceIdHex)
              response.putHeaders(traceIdHeader)
            }
          }
      }
    }
  }
}

object ServerMiddleware extends ServerMiddleware
