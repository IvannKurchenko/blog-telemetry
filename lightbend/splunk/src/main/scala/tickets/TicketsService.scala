package tickets

import scala.concurrent.ExecutionContext

class TicketsService(configuration: TicketsConfiguration)
                    (implicit ec: ExecutionContext) {
  private val repo = new TicketsRepo()
  private val elastic = new TicketsElasticRepo(configuration.elasticsearch)

}
