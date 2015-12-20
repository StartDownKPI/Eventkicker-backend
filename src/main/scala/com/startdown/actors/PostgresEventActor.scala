package com.startdown.actors

import akka.actor.{Props, Actor}
import akka.pattern.{ask, pipe}
import com.startdown.models.{EventDao, Event}
import com.startdown.server.WebService
import com.startdown.utils.{Response, Responsive, CRUD}
import spray.json._

import scala.concurrent.Future

/**
  * infm created it with love on 11/8/15. Enjoy ;)
  */
trait EventService extends WebService {
  import com.startdown.models.EventJsonProtocol._
  import spray.httpx.SprayJsonSupport._

  val postgresEventWorker = actorRefFactory.actorOf(Props[PostgresEventActor],
    "postgres-event-worker")

  def postgresEventCall(message: Any) =
    (postgresEventWorker ? message).mapTo[String].map(identity)

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

object PostgresEventActor extends CRUD[Event, Long]

class PostgresEventActor extends Actor with Responsive[Event] {
  import PostgresEventActor._
  import com.startdown.models.EventJsonProtocol._
  import context.dispatcher

  implicit val responseFormat = jsonFormat4(Response[Event])

  override def receive = {
    case FetchAll =>
      makeResponse(EventDao.listAllEvents) pipeTo sender

    case Create(e: Event) =>
      makeResponse(EventDao.addEvent(e)) pipeTo sender

    case Read(id: Long) =>
      makeResponse(EventDao.findEvent(id)) pipeTo sender

    case Update(e: Event) =>
      makeResponse(EventDao.updateEvent(e)) pipeTo sender

    case Delete(id: Long) =>
      makeResponse(EventDao.deleteEvent(id)) pipeTo sender

    case DeleteAll =>
      makeResponse(EventDao.deleteAll) pipeTo sender

    case CreateTable =>
      makeResponse(EventDao.createTable.map(_.toJson.compactPrint)) pipeTo sender

    case DropTable =>
      makeResponse(EventDao.dropTable.map(_.toJson.compactPrint)) pipeTo sender
  }
}