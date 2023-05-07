package tickets
import com.comcast.ip4s.{Host, Port}
import pureconfig.generic.auto._
import pureconfig.{ConfigReader, ConfigSource}
import pureconfig.error.UserValidationFailed

case class ElasticsearchConfiguration(url: String)

case class PostgreConfiguration(url: String)

case class KafkaConfiguration(url: String, topic: String)

case class ApplicationConfiguration(host: String, port: Int)

case class ProjectsServiceConfiguration(url: String)

case class TicketsConfiguration(elasticsearch: ElasticsearchConfiguration,
                                postgre: PostgreConfiguration,
                                kafka: KafkaConfiguration,
                                projects: ProjectsServiceConfiguration,
                                application: ApplicationConfiguration)

object TicketsConfiguration {
  def load: TicketsConfiguration = ConfigSource.default.loadOrThrow[TicketsConfiguration]
}
