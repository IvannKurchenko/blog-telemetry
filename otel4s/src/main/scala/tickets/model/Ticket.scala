package tickets.model

case class Ticket(id: Long,
                  project: Long,
                  title: String,
                  description: String,
                  createdAt: Long,
                  createdBy: String,
                  modifiedAt: Long,
                  modifiedBy: String) {
  def toSearch: SearchTicket = {
    SearchTicket(id, project, title, description)
  }
}

case class TicketEvent(event: String, ticket: Ticket)


case class SearchTicket(id: Long,
                        project: Long,
                        title: String,
                        description: String)


case class CreateTicket(project: Long,
                        title: String,
                        description: String,
                        creator: String)

case class UpdateTicket(id: Long,
                        project: Long,
                        title: String,
                        description: String,
                        updater: String)

