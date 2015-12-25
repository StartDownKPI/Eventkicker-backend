package com.startdown.actors

import akka.actor.{Actor, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.startdown.models.{User, UserDao}
import com.startdown.server.{WebService, Authenticator, AuthInfo}
import com.startdown.utils.{CRUD, Response, Responsive}
import spray.http.StatusCodes
import spray.json._

import scala.concurrent.duration._

/**
  * infm created it with love on 11/7/15. Enjoy ;)
  */

trait UserService extends WebService {

  import com.startdown.models.UserJsonProtocol._
  import com.startdown.server.Authenticator._
  import spray.httpx.SprayJsonSupport._

  val postgresUserWorker = actorRefFactory.actorOf(Props[PostgresUserActor],
    "postgres-user-worker")

  def postgresUserCall(message: Any) =
    (postgresUserWorker ? message).mapTo[String].map(identity)

  val userServiceRoutes = {
    import PostgresUserActor._
    pathPrefix("login") {
      pathEndOrSingleSlash {
        authenticate(basicUserAuthenticator) { authInfo =>
          post {
            complete {
              authInfo.user.toJson.compactPrint
            }
          }
        }
      }
    } ~
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
          } ~
          pathPrefix(LongNumber) { userId =>
            pathEndOrSingleSlash {
              get {
                complete {
                  postgresUserCall(Read(userId))
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
                      postgresUserCall(Delete(userId))
                    }
                  }
            } ~
                pathPrefix("events") {
                  pathEndOrSingleSlash {
                    get {
                      complete {
                        postgresUserCall(GetEvents(userId))
                      }
                    }
                  }
                }
          }
    }
  }
}

object PostgresUserActor extends CRUD[User, Long] {
  case class GetEvents(userId: Long)
}

class PostgresUserActor extends Actor with Responsive[User] {

  import PostgresUserActor._
  import com.startdown.models.UserJsonProtocol._
  import context.dispatcher

  implicit val responseFormat = jsonFormat4(Response[User])

  override def receive = {
    case FetchAll =>
      makeResponse(UserDao.listAllUsers) pipeTo sender

    case Create(u: User) =>
      makeResponse(UserDao.addUser(u)) pipeTo sender

    case Read(userId: Long) =>
      makeResponse(UserDao.findUser(userId)) pipeTo sender

    case Update(u: User) =>
      makeResponse(UserDao.updateUser(u)) pipeTo sender

    case Delete(userId: Long) =>
      makeResponse(UserDao.deleteUser(userId)) pipeTo sender

    case DeleteAll =>
      makeResponse(UserDao.deleteAll) pipeTo sender

    case CreateTable =>
      makeResponse(UserDao.createTable.map(_.toJson.compactPrint)) pipeTo sender

    case DropTable =>
      makeResponse(UserDao.dropTable.map(_.toJson.compactPrint)) pipeTo sender

    case GetEvents(userId: Long) =>
      implicit val timeout = Timeout(120.seconds)
      context.actorSelection("../postgres-event-worker") ? PostgresEventActor
          .GetForUser(userId) pipeTo sender
  }
}
