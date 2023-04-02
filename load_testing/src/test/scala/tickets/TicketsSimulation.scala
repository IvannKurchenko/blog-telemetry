package tickets

import io.gatling.core.Predef._
import io.gatling.core.body.StringBody
import io.gatling.http.Predef._
import io.gatling.core.scenario.Simulation

import io.circe.generic.auto._
import io.circe.syntax._

import java.nio.charset.Charset
import scala.concurrent.duration._
import scala.language.postfixOps


class TicketsSimulation extends Simulation {

  val httpProtocol = http.baseUrl("http://localhost:10001").contentTypeHeader("application/json")
  val users = 10
  val tickets = 1 to 20
  val projects = 1 to 10

  def execCreateTicket(id: Int, projectId: Long) = {
    val body = CreateTicket(
      project = projectId,
      title = s"Test ticket title $id",
      description = s"Test ticket description: $id.",
      creator = "john.doe@acme.com"
    )

    exec {
      http(s"Create project ticket: $projectId")
        .post("/tickets")
        .body(StringBody(body.asJson.noSpaces, Charset.forName("UTF-8")))
        .header("Content-Type", "application/json")
        .check(status is 200)
        .requestTimeout(1 minute)
    }
  }

  def execCreateTickets = {
    for {
      ticketId <- tickets
      projectId <- projects
    } yield execCreateTicket(ticketId, projectId)
  }


  def execSearchProjectTickets(project: Long) = {
    exec {
      http(s"Search project tickets: $project")
        .get(s"/tickets?search=test%20ticket&project=$project")
        .check(status is 200)
        .requestTimeout(1 minute)
    }
  }

  def execSearchTickets = {
    for {
      projectId <- projects
    } yield execSearchProjectTickets(projectId)
  }

  def execUpdateTicket(id: Int, projectId: Long) = {
    val body = UpdateTicket(
      id = id,
      project = projectId,
      title = s"Updated test ticket title $id",
      description = s"Updated test ticket description: $id.",
      updater = "john.doe@acme.com"
    )

    exec {
      http(s"Update ticket: $projectId")
        .put("/tickets")
        .body(StringBody(body.asJson.noSpaces, Charset.forName("UTF-8")))
        .header("Content-Type", "application/json")
        .check(status is 200)
    }
  }

  def execUpdateTickets = {
    for {
      ticketId <- tickets
      projectId <- projects
    } yield execUpdateTicket(ticketId, projectId)
  }

  def execDeleteTicket(id: Int) = {
    exec {
      http(s"Delete $id")
        .delete(s"/tickets/$id")
    }
  }

  def execDeleteTickets = {
    for {
      ticketId <- 1 to 30
    } yield execDeleteTicket(ticketId)
  }

  setUp(
    scenario("Simulate users traffic").
      exec(execCreateTickets).
      exec(execSearchTickets).
      exec(execUpdateTickets).
      exec(execDeleteTickets).
      inject(atOnceUsers(users)).
      protocols(httpProtocol)
  )
}
