package com.startdown.actors

import akka.actor.{Props, Actor}
import akka.pattern.{ask, pipe}
import com.startdown.models.{Item, ItemDao}
import com.startdown.server.WebService
import com.startdown.utils.{CRUD, Response, Responsive}
import spray.json._

/**
  * infm created it with love on 12/14/15. Enjoy ;)
  */
trait ItemService extends WebService {
  import com.startdown.models.ItemJsonProtocol._
  import spray.httpx.SprayJsonSupport._

  val postgresItemWorker = actorRefFactory.actorOf(Props[PostgresItemActor],
    "postgres-item-worker")

  def postgresItemCall(message: Any) =
    (postgresItemWorker ? message).mapTo[String].map(identity)

  val itemServiceRoutes = {
    import PostgresItemActor._
    pathPrefix("items") {
      pathEndOrSingleSlash {
        get {
          complete {
            postgresItemCall(FetchAll)
          }
        } ~
            post {
              entity(as[Item]) { item =>
                complete {
                  postgresItemCall(Create(item))
                }
              }
            } ~
            delete {
              complete {
                postgresItemCall(DeleteAll)
              }
            }
      } ~
          path("table") {
            get {
              complete {
                postgresItemCall(CreateTable)
              }
            } ~
                delete {
                  complete {
                    postgresItemCall(DropTable)
                  }
                }
          } ~
          path(LongNumber) { itemId =>
            get {
              complete {
                postgresItemCall(Read(itemId))
              }
            } ~
                put {
                  entity(as[Item]) { item =>
                    complete {
                      postgresItemCall(Update(item))
                    }
                  }
                } ~
                delete {
                  complete {
                    postgresItemCall(Delete(itemId))
                  }
                }
          }
    }
  }
}

object PostgresItemActor extends CRUD[Item, Long]

class PostgresItemActor extends Actor with Responsive[Item] {

  import PostgresItemActor._
  import com.startdown.models.ItemJsonProtocol._
  import context.dispatcher

  implicit val responseFormat = jsonFormat4(Response[Item])

  override def receive = {
    case FetchAll =>
      makeResponse(ItemDao.listAllItems) pipeTo sender

    case Create(i: Item) =>
      makeResponse(ItemDao.addItem(i)) pipeTo sender

    case Read(id: Long) =>
      makeResponse(ItemDao.findItem(id)) pipeTo sender

    case Update(i: Item) =>
      makeResponse(ItemDao.updateItem(i)) pipeTo sender

    case Delete(id: Long) =>
      makeResponse(ItemDao.deleteItem(id)) pipeTo sender

    case DeleteAll =>
      makeResponse(ItemDao.deleteAll) pipeTo sender

    case CreateTable =>
      makeResponse(ItemDao.createTable.map(_.toJson.compactPrint)) pipeTo sender

    case DropTable =>
      makeResponse(ItemDao.dropTable.map(_.toJson.compactPrint)) pipeTo sender
  }
}
