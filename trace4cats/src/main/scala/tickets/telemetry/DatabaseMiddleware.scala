package tickets.telemetry

import doobie.util.transactor.Strategy

trait DatabaseMiddleware {
  Strategy(
    before = { _ =>
      println("before")
      ()
    },
    after = { _ =>
      println("after")
      ()
    },
    onSuccess = { (_, _) =>
      println("success")
      ()
    },
    onFailure = { (_, _) =>
      println("failure")
      ()
    }
  )
}

object DatabaseMiddleware extends DatabaseMiddleware
