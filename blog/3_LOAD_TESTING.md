### Simulating user traffic 
To get some data for any telemetry from `ticket-service` we need to send number of requests. 
I decided to use [Gatling](https://gatling.io) for this. Despite the fact that this is rather load and performance
testing tool, let's omit testing aspect and use it just to simulate user traffic.
Following scenario for single user is used to put some load on service:
- Create 20 tickets for 10 different projects;
- Search tickets for 10 different projects
- Update 20 tickets;
- delete 30 tickets;