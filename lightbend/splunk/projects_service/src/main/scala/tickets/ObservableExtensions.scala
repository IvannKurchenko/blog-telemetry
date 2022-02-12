package tickets

import akka.stream.Materializer
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import org.mongodb.scala.Observable

import scala.concurrent.{ExecutionContext, Future}

object ObservableExtensions {
  implicit class ObservableOps[T](underlying: Observable[T]) {
    def all(implicit materializer: Materializer, ec: ExecutionContext): Future[List[T]] = {
      Source.fromPublisher(underlying).runWith(Sink.collection).map(_.toList)
    }

    def headOption(implicit materializer: Materializer, ec: ExecutionContext): Future[Option[T]] = {
      all.map(_.headOption)
    }

    def head(implicit materializer: Materializer, ec: ExecutionContext): Future[T] = {
      all.map(_.head)
    }
  }
}
