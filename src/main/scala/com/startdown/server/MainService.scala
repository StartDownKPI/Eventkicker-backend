package com.startdown.server

import akka.actor.Props
import akka.pattern.ask
import com.startdown.actors.PostgresUserActor
import com.startdown.models.User
import spray.http.MediaTypes._
import spray.httpx.marshalling.marshal

/**
  * infm created it with love on 11/7/15. Enjoy ;)
  */

trait MainService extends WebService {

  import com.startdown.models.UserJsonProtocol._
  import spray.httpx.SprayJsonSupport._

  val exampleRoute =
    path("api") {
      get {
        respondWithMediaType(`application/json`) {
          complete {
            marshal(User("infm", "Illia", "qwerty", 100))
          }
        }
      }
    }

  import PostgresUserActor._

  val postgresWorker = actorRefFactory.actorOf(Props[PostgresUserActor],
    "postgres-worker")

  def postgresCall(message: Any) =
    (postgresWorker ? message).mapTo[String].map(identity)

  val userServiceRoutes = {
    pathPrefix("users") {
      path("") {
        get {
          respondWithMediaType(`application/json`) {
            complete {
              postgresCall(FetchAll)
            }
          }
        }
      }
    }
  }
}
