package tickets

case class ElasticsearchConfiguration(url: String)

case class PostgreConfiguration()

case class KafkaConfiguration()

case class ApplicationConfiguration(host: String, port: Int)

case class TicketsConfiguration(elasticsearch: ElasticsearchConfiguration,
                                postgre: PostgreConfiguration,
                                kafka: KafkaConfiguration,
                                application: ApplicationConfiguration)
