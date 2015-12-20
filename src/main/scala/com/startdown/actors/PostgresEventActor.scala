package com.startdown.actors

import akka.actor.{Actor, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.startdown.models.{Event, EventDao}
import com.startdown.server.WebService
import com.startdown.utils.{CRUD, Response, Responsive}
import spray.json._

import scala.concurrent.duration._

/**
  * infm created it with love on 11/8/15. Enjoy ;)
  */
case class SearchQuery(keywords: List[String])
object SearchQueryJsonProtocol extends DefaultJsonProtocol {
  implicit val searchQueryFormat = jsonFormat1(SearchQuery)
}

trait EventService extends WebService {
  import com.startdown.models.EventJsonProtocol._
  import com.startdown.actors.SearchQueryJsonProtocol._
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
          } ~
          path("search") {
            get {
              parameter('keywords) { keywords =>
                complete {
                  postgresEventCall(Search(keywords.split(",").toList))
                }
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

object PostgresEventActor extends CRUD[Event, Long] {
  case class Search(keywords: List[String])
}

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

    case Search(keywords: List[String]) => {
      implicit val timeout = Timeout(120.seconds)
      makeResponse(EventDao.searchEvents(keywords).map {
        seq => if (seq.nonEmpty) seq else None
      }) pipeTo sender
    }
  }
}