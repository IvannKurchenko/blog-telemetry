package tickets

case class Ticket(id: Long,
                  project: Long,
                  title: String,
                  description: String,
                  createdAt: Long,
                  createdBy: String,
                  modifiedAt: Long,
                  modifiedBy: String) {
  def toSearch: SearchTicket = {
    SearchTicket(id, title, description)
  }
}


case class SearchTicket(id: Long,
                        title: String,
                        description: String)


case class CreateTicket(project: Long,
                        title: String,
                        description: String,
                        creator: String)

