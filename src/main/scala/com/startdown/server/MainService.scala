package com.startdown.server

import akka.actor.Props
import akka.pattern.ask
import com.startdown.actors.{PostgresEventActor, PostgresUserActor}
import com.startdown.models.{Event, User}

/**
  * infm created it with love on 11/7/15. Enjoy ;)
  */

trait MainService extends WebService {

  import com.startdown.models.UserJsonProtocol._
  import com.startdown.models.EventJsonProtocol._
  import spray.httpx.SprayJsonSupport._

  val postgresUserWorker = actorRefFactory.actorOf(Props[PostgresUserActor],
    "postgres-user-worker")

  def postgresUserCall(message: Any) =
    (postgresUserWorker ? message).mapTo[String].map(identity)

  val postgresEventWorker = actorRefFactory.actorOf(Props[PostgresEventActor],
    "postgres-event-worker")

  def postgresEventCall(message: Any) =
    (postgresEventWorker ? message).mapTo[String].map(identity)

  val userServiceRoutes = {
    import PostgresUserActor._
    pathPrefix("users") {
      pathEndOrSingleSlash {
        get {
          complete {
            postgresUserCall(FetchAll)
          }
        } ~
          post {
            entity(as[User]) { user =>
              complete {
                postgresUserCall(Create(user))
              }
            }
          } ~
          delete {
            complete {
              postgresUserCall(DeleteAll)
            }
          }
      } ~
        path("table") {
          get {
            complete {
              postgresUserCall(CreateTable)
            }
          } ~
            delete {
              complete {
                postgresUserCall(DropTable)
              }
            }
        }
    } ~
      path("user" / Segment) { username =>
        get {
          complete {
            postgresUserCall(Read(username))
          }
        } ~
          put {
            entity(as[User]) { user =>
              complete {
                postgresUserCall(Update(user))
              }
            }
          } ~
          delete {
            complete {
              postgresUserCall(Delete(username))
            }
          }
      }
  }


  val eventServiceRoutes = {
    import PostgresEventActor._
    pathPrefix("events") {
      pathEndOrSingleSlash {
        get {
          complete {
            postgresEventCall(FetchAll)
          }
        } ~
          post {
            entity(as[Event]) { event =>
              complete {
                postgresEventCall(Create(event))
              }
            }
          } ~
          delete {
            complete {
              postgresEventCall(DeleteAll)
            }
          }
      } ~
        path("table") {
          get {
            complete {
              postgresEventCall(CreateTable)
            }
          } ~
            delete {
              complete {
                postgresEventCall(DropTable)
              }
            }
        }
    } ~
      path("event" / LongNumber) { eventId =>
        get {
          complete {
            postgresEventCall(Read(eventId))
          }
        } ~
          put {
            entity(as[Event]) { event =>
              complete {
                postgresEventCall(Update(event))
              }
            }
          } ~
          delete {
            complete {
              postgresEventCall(Delete(eventId))
            }
          }
      }
  }
}
