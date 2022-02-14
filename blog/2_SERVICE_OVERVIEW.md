### Disclaimer
For the sake example, task tickets system is much simplified comparing to real world production system and
does not include users management, authorisation and authentication, email service is stubbed etc.

Along with that, tech stack has been chosen to be more or less diverse to simulate real production system to some extent,
hence certain architectural decisions might not look perfect.

### Domain model
Ticket - task ticket
Project - tickets leaves in ticket system, it relates 


### Architecture overview
Task ticketing system which we are going to monitor looks following at high level:

![](images/system-architecture.drawio.png)

- `tickets-service` - micro-service for tasks tickets, also provides full text search capabilities, publish change to kafka topic
- `notification-service` - micro-service for user subscriptions, send emails for subscribed tickets.
- `projects-service` - micro-service for projects, which tickets are leave in.

We will focus on tickets service monitoring

### `tickets-service` API
<br>`GET /v1/tickets?search={search-query}&project={}` - performs full text search over all tickets
<br>`POST /v1/tickets` - create single ticket
<br>`PUT /v1/tickets/:id` - update single ticket
<br>`DELETE /v1/tickets/:id` - delete single ticket

Example ticket model is following: 
```json
{
  "id": 1,
  "project": 2, 
  "title": "ticket title",
  "description": "ticket title",
  "createdAt": 1644560213,
  "createdBy": "john.doe@acme.com",
  "modifiedAt": 1644560213,
  "modifiedBy": "john.doe@acme.com"
}
```
