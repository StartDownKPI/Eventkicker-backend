package com.startdown.actors

import akka.actor.{Actor, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.startdown.models.{Event, EventDao, EventWithAuthorName}
import com.startdown.server.WebService
import com.startdown.utils.{CRUD, Response, Responsive}
import spray.json._

import scala.concurrent.Future
import scala.concurrent.duration._

/**
  * infm created it with love on 11/8/15. Enjoy ;)
  */
trait EventService extends WebService {
  import com.startdown.models.EventJsonProtocol._
  import com.startdown.server.Authenticator._
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
          } ~
          pathPrefix("named") {
            pathEndOrSingleSlash {
              get {
                complete {
                  postgresEventCall(FetchAllWithAuthorNames)
                }
              }
            }
          } ~
          pathPrefix(LongNumber) { eventId =>
            pathEndOrSingleSlash {
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
            } ~
                pathPrefix("items") {
                  pathEndOrSingleSlash {
                    get {
                      complete {
                        postgresEventCall(GetItems(eventId))
                      }
                    }
                  }
                } ~
                pathPrefix("author") {
                  pathEndOrSingleSlash {
                    get {
                      complete {
                        postgresEventCall(GetAuthor(eventId))
                      }
                    }
                  }
                } ~
                pathPrefix("comments") {
                  pathEndOrSingleSlash {
                    get {
                      complete {
                        postgresEventCall(GetComments(eventId))
                      }
                    }
                  } ~
                      pathPrefix("preview" / LongNumber) { limit =>
                        pathEndOrSingleSlash {
                          get {
                            complete {
                              postgresEventCall(GetCommentsPreview(eventId,
                                limit))
                            }
                          }
                        }
                      }
                } ~
                pathPrefix("likes") {
                  pathPrefix("done") {
                    pathEndOrSingleSlash {
                      authenticate(basicUserAuthenticator) { authInfo =>
                        get {
                          complete {
                            postgresEventCall(
                              IsLiked(eventId, authInfo.user.id.get))
                          }
                        }
                      }
                    }
                  } ~
                      pathPrefix("count") {
                        pathEndOrSingleSlash {
                          get {
                            complete {
                              postgresEventCall(GetLikesCount(eventId))
                            }
                          }
                        }
                      }
                }
          }
    }
  }
}

object PostgresEventActor extends CRUD[Event, Long] {
  case object FetchAllWithAuthorNames
  case class Search(keywords: List[String])
  case class GetForUser(userId: Long)
  case class GetItems(id: Long)
  case class GetComments(id: Long)
  case class GetCommentsPreview(id: Long, limit: Long)
  case class GetAuthor(id: Long)
  case class IsLiked(eventId: Long, authorId: Long)
  case class GetLikesCount(eventId: Long)
}

class PostgresEventActor extends Actor with Responsive[Event] {
  import PostgresEventActor._
  import com.startdown.models.EventJsonProtocol._
  import context.dispatcher

  implicit val responseFormat = jsonFormat4(Response[Event])
  implicit val response1Format = jsonFormat4(Response[EventWithAuthorName])

  implicit val timeout = Timeout(120.seconds)
  override def receive = {
    case FetchAll =>
      makeResponse(EventDao.listAllEvents) pipeTo sender

    case FetchAllWithAuthorNames =>
      type T = EventWithAuthorName
      EventDao.listAllEventsWithAuthorNames.map {
        case multiple: Seq[T] =>
          new Response[T](true, multiple = Some(multiple))
        case _ => new Response[T](false)
      }.recover {
        case cause => new Response[T](false, message = Some(cause.toString))
      }.map { case r => r.toJson.compactPrint } pipeTo sender

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

    case Search(keywords: List[String]) =>
      makeResponse(EventDao.searchEvents(keywords).map {
        seq => if (seq.nonEmpty) seq else None
      }) pipeTo sender

    case GetForUser(userId: Long) =>
      makeResponse(EventDao.getForUser(userId)) pipeTo sender

    case GetItems(eventId: Long) =>
      context.actorSelection("../postgres-item-worker") ? PostgresItemActor
          .GetForEvent(eventId) pipeTo sender

    case GetComments(eventId: Long) =>
      context.actorSelection("../postgres-comment-worker") ? PostgresCommentActor
          .GetForEvent(eventId) pipeTo sender

    case GetCommentsPreview(eventId: Long, limit: Long) =>
      context.actorSelection("../postgres-comment-worker") ? PostgresCommentActor
          .GetPreviewForEvent(eventId, limit) pipeTo sender

    case GetAuthor(eventId: Long) =>
      def getAuthor0(authorId: Long) =
        context.actorSelection("../postgres-user-worker") ? PostgresUserActor
            .Read(authorId)
      EventDao.findEvent(eventId).flatMap {
        case Some(x) => getAuthor0(x.authorId.get)
        case _ => makeResponse(Future { None })
      } recoverWith {
        case t: Throwable => makeResponse(Future { t })
      } pipeTo sender

    case IsLiked(eventId: Long, authorId: Long) =>
      context.actorSelection("../postgres-like-worker") ? PostgresLikeActor
          .FindForEvent(eventId, authorId) pipeTo sender

    case GetLikesCount(eventId: Long) =>
      context.actorSelection("../postgres-like-worker") ? PostgresLikeActor
          .CountForEvent(eventId) pipeTo sender
  }
}