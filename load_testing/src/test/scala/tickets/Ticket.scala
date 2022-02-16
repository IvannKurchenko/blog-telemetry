package tickets

case class CreateTicket(project: Long,
                        title: String,
                        description: String,
                        creator: String)

case class UpdateTicket(id: Long,
                        project: Long,
                        title: String,
                        description: String,
                        updater: String)
