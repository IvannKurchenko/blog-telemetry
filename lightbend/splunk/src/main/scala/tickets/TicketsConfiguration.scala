package tickets

case class ElasticsearchConfiguration()

case class PostgreConfiguration()

case class KafkaConfiguration()

case class ApplicationConfiguration(host: String, port: Int)

case class TicketsConfiguration(elasticsearch: ElasticsearchConfiguration,
                                postgre: PostgreConfiguration,
                                kafka: KafkaConfiguration,
                                application: ApplicationConfiguration)
