package com.startdown.actors

import akka.actor.Actor
import akka.pattern.pipe
import com.startdown.models.{Item, ItemDao}
import com.startdown.utils.{CRUD, Response, Responsive}
import spray.json._

/**
  * infm created it with love on 12/14/15. Enjoy ;)
  */
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
