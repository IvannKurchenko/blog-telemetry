
### Scala stacks 
In series of articles we are going to review number of Application Performance Monitoring (APM) and telemetry solutions
which could be used in different Scala ecosystem.

In particular, following APM solutions are considered:
- Splunk APM;
- Sentry APM;
- Datalog APM;
- NewRelic

### Disclaimer
For the sake example, task tickets system is much simplified comparing to real world production system and
does not include users management, authorisation and authentication, email service is stubbed etc.

Along with that, tech stack has been chosen to be more or less diverse to simulate real production system to some extent,
hence certain architectural decisions might not look perfect.

### Architecture overview
Task ticketing system which we are going to monitor looks following at high level:

![](images/system-architecture.drawio.png)

- `tickets-service` - micro-service for tasks tickets, also provides full text search capabilities, publish change to kafka topic
- `notification-service` - micro-service for user subscriptions, send emails for subscribed tickets.
- `projects-service` - micro-service for projects, which tickets are leave in.


#### `project-service` API
<br>`GET /v1/projects` - fetch all projects
<br>`POST /v1/projects` - create new project
<br>`DELETE /v1/projects/:id` - delete new project

Example ticket model is following:
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "name": "ticket title",
  "description": "ticket title",
  "users": ["john.doe@acme.com"],
  "createdAt": 1644560213,
  "createdBy": 1,
  "modifiedAt": 1644560213,
  "modifiedBy": 1
}
```

#### `tickets-service` API
<br>`GET /v1/tickets` - fetch all tickets
<br>`GET /v1/tickets?search={search-query}&project={}` - performs full text search over all tickets
<br>`POST /v1/tickets` - create single ticket
<br>`PUT /v1/tickets/:id` - update single ticket
<br>`DELETE /v1/tickets` - delete single ticket

Example ticket model is following: 
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "project": "123e4567-e89b-12d3-a456-426614174000", 
  "title": "ticket title",
  "description": "ticket title",
  "createdAt": 1644560213,
  "createdBy": "john.doe@acme.com",
  "modifiedAt": 1644560213,
  "modifiedBy": "john.doe@acme.com"
}
```

#### `notification-service` API
User can subscribe on particular ticket or for entire project. 
<br>`GET /v1/subsctiptions/:user-id` - fetch all subscription for user
<br>`POST /v1/subsctiptions/:user-id` - create subscription for user
<br>`DELETE /v1/subsctiptions/:user-id` - create subscription for user

Example subscription ticket model is following:
```json
{
  "id": 1,
  "subscriptionItemType": "project",
  "subscriptionItemId": 1
}
```
