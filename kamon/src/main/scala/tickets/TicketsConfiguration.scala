package tickets

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
