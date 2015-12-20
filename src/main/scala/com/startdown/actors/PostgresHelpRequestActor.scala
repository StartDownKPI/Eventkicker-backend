package com.startdown.actors

import akka.actor.{Actor, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.startdown.actors.EmailActor.Send
import com.startdown.models.{HelpRequest, HelpRequestDao}
import com.startdown.server.WebService
import com.startdown.utils.{CRUD, Response, Responsive}
import spray.json._

import scala.concurrent.duration._

/**
  * infm created it with love on 12/16/15. Enjoy ;)
  */
case class HelpSuggest(senderId: Long,
                       destinationUsername: String,
                       description: Option[String],
                       eventId: Long,
                       itemIds: List[Long])
object HelpSuggestJsonProtocol extends DefaultJsonProtocol {
  implicit val helpSuggestFormat = jsonFormat5(HelpSuggest)
}

object PostgresHelpRequestActor extends CRUD[HelpRequest, Long] {
  case class Submit(helpSuggest: HelpSuggest)
}

trait HelpRequestService extends WebService {
  import com.startdown.actors.HelpSuggestJsonProtocol._
  import com.startdown.models.HelpRequestJsonProtocol._
  import spray.httpx.SprayJsonSupport._

  val postgresHelpRequestWorker = actorRefFactory.actorOf(
    Props[PostgresHelpRequestActor], "postgres-help-request-worker")

  def postgresHelpRequestCall(message: Any) =
    (postgresHelpRequestWorker ? message).mapTo[String].map(identity)

  val helpRequestServiceRoutes = {
    import PostgresHelpRequestActor._
    pathPrefix("help-requests") {
      pathEndOrSingleSlash {
        get {
          complete {
            postgresHelpRequestCall(FetchAll)
          }
        } ~
            post {
              entity(as[HelpRequest]) { helpRequest =>
                complete {
                  postgresHelpRequestCall(Create(helpRequest))
                }
              }
            } ~
            delete {
              complete {
                postgresHelpRequestCall(DeleteAll)
              }
            }
      } ~
          path("table") {
            get {
              complete {
                postgresHelpRequestCall(CreateTable)
              }
            } ~
                delete {
                  complete {
                    postgresHelpRequestCall(DropTable)
                  }
                }
          }
    } ~
        path("help-request" / LongNumber) { helpRequestId =>
          get {
            complete {
              postgresHelpRequestCall(Read(helpRequestId))
            }
          } ~
              put {
                entity(as[HelpRequest]) { helpRequest =>
                  complete {
                    postgresHelpRequestCall(Update(helpRequest))
                  }
                }
              } ~
              delete {
                complete {
                  postgresHelpRequestCall(Delete(helpRequestId))
                }
              }
        } ~
        path("help-request" / "submit") {
          post {
            entity(as[HelpSuggest]) { helpSuggest =>
              complete {
                postgresHelpRequestCall(Submit(helpSuggest))
              }
            }
          }
    }
  }
}

class PostgresHelpRequestActor extends Actor with Responsive[HelpRequest] {
  import PostgresHelpRequestActor._
  import com.startdown.models.HelpRequestJsonProtocol._
  import context.dispatcher

  implicit val responseFormat = jsonFormat4(Response[HelpRequest])
  val emailActor = context.actorOf(Props[EmailActor], "email-worker")

  override def receive = {
    case FetchAll =>
      makeResponse(HelpRequestDao.listAllHelpRequests) pipeTo sender

    case Create(hr: HelpRequest) =>
      makeResponse(HelpRequestDao.addHelpRequest(hr)) pipeTo sender

    case Submit(hs: HelpSuggest) => {
      implicit val timeout = Timeout(60.seconds)
/*
      val helpRequests = for {
        id <- hs.itemIds
        f <- self ? Create(new HelpRequest(authorId = Some(hs.senderId),
          description = hs.description, itemId = Some(id)))
        obj <- f
        if obj.isInstanceOf[HelpRequest]
      } yield List(obj)
*/
      val itemIds = hs.itemIds map { id =>
        self ? Create(new HelpRequest(authorId = Some(hs.senderId),
          description = hs.description, itemId = Some(id)))
      } filter {
        case hr: HelpRequest => true
        case _ => false
      } map {
        case hr: HelpRequest => hr.itemId.get
      } mkString ", "
      val emailFuture = emailActor ? Send(hs.destinationUsername,
        s"HelpSuggest from ${hs.senderId} for ${hs.eventId}",
        s"""Hey yo! I've heard that this event gonna be crazy, so
            |plz, take this help: $itemIds""".stripMargin)
      makeResponse(emailFuture) pipeTo sender
    }

    case Read(id: Long) =>
      makeResponse(HelpRequestDao.findHelpRequest(id)) pipeTo sender

    case Update(hr: HelpRequest) =>
      makeResponse(HelpRequestDao.updateHelpRequest(hr)) pipeTo sender

    case Delete(id: Long) =>
      makeResponse(HelpRequestDao.deleteHelpRequest(id)) pipeTo sender

    case DeleteAll =>
      makeResponse(HelpRequestDao.deleteAll) pipeTo sender

    case CreateTable =>
      makeResponse(HelpRequestDao.createTable.map(_.toJson.compactPrint)) pipeTo sender

    case DropTable =>
      makeResponse(HelpRequestDao.dropTable.map(_.toJson.compactPrint)) pipeTo sender
  }

}