package com.startdown.actors

import akka.actor.{Props, Actor}
import akka.pattern.{ask, pipe}
import com.startdown.models.{Like, LikeDao}
import com.startdown.server.WebService
import com.startdown.utils.{CRUD, Response, Responsive}
import spray.json._

/**
  * infm created it with love on 12/16/15. Enjoy ;)
  */
trait LikeService extends WebService {
  import com.startdown.models.LikeJsonProtocol._
  import spray.httpx.SprayJsonSupport._

  val postgresLikeWorker = actorRefFactory.actorOf(Props[PostgresLikeActor],
    "postgres-like-worker")

  def postgresLikeCall(message: Any) =
    (postgresLikeWorker ? message).mapTo[String].map(identity)


  val likeServiceRoutes = {
    import PostgresLikeActor._
    pathPrefix("likes") {
      pathEndOrSingleSlash {
        get {
          complete {
            postgresLikeCall(FetchAll)
          }
        } ~
            post {
              entity(as[Like]) { like =>
                complete {
                  postgresLikeCall(Create(like))
                }
              }
            } ~
            delete {
              complete {
                postgresLikeCall(DeleteAll)
              }
            }
      } ~
          path("table") {
            get {
              complete {
                postgresLikeCall(CreateTable)
              }
            } ~
                delete {
                  complete {
                    postgresLikeCall(DropTable)
                  }
                }
          } ~
          path(LongNumber) { likeId =>
            get {
              complete {
                postgresLikeCall(Read(likeId))
              }
            } ~
                put {
                  entity(as[Like]) { like =>
                    complete {
                      postgresLikeCall(Update(like))
                    }
                  }
                } ~
                delete {
                  complete {
                    postgresLikeCall(Delete(likeId))
                  }
                }
          }
    }
  }
}

object PostgresLikeActor extends CRUD[Like, Long]

class PostgresLikeActor extends Actor with Responsive[Like] {

  import PostgresLikeActor._
  import com.startdown.models.LikeJsonProtocol._
  import context.dispatcher

  implicit val responseFormat = jsonFormat4(Response[Like])

  override def receive = {
    case FetchAll =>
      makeResponse(LikeDao.listAllLikes) pipeTo sender

    case Create(i: Like) =>
      makeResponse(LikeDao.addLike(i)) pipeTo sender

    case Read(id: Long) =>
      makeResponse(LikeDao.findLike(id)) pipeTo sender

    case Update(l: Like) =>
      sender ! new Response[Like](false,
        message = Some("Like can't be updated")).toJson.compactPrint

    case Delete(id: Long) =>
      makeResponse(LikeDao.deleteLike(id)) pipeTo sender

    case DeleteAll =>
      makeResponse(LikeDao.deleteAll) pipeTo sender

    case CreateTable =>
      makeResponse(LikeDao.createTable.map(_.toJson.compactPrint)) pipeTo sender

    case DropTable =>
      makeResponse(LikeDao.dropTable.map(_.toJson.compactPrint)) pipeTo sender
  }
}