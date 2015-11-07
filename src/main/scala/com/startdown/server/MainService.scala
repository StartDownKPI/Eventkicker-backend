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
  import PostgresUserActor._

  val postgresWorker = actorRefFactory.actorOf(Props[PostgresUserActor],
    "postgres-worker")

  def postgresCall(message: Any) =
    (postgresWorker ? message).mapTo[String].map(identity)

  val userServiceRoutes = {
    pathPrefix("users") {
      pathEndOrSingleSlash {
        get {
          complete {
            postgresCall(FetchAll)
          }
        } ~
          post {
            entity(as[User]) { user =>
              complete {
                postgresCall(Create(user))
              }
            }
          }
      }
    } ~
      path("user" / Segment) { username =>
        get {
          complete {
            postgresCall(Read(username))
          }
        } ~
          put {
            entity(as[User]) { user =>
              complete {
                postgresCall(Update(user))
              }
            }
          } ~
          delete {
            complete {
              postgresCall(Delete(username))
            }
          }
      }
  }
}
