package com.startdown.actors

import akka.actor.Actor
import akka.pattern.pipe
import com.startdown.models.{ItemDao, Item}
import com.startdown.utils.CRUD
import spray.json._

import scala.concurrent.Future

/**
  * infm created it with love on 12/14/15. Enjoy ;)
  */
object PostgresItemActor extends CRUD[Item, Long]

class PostgresItemActor extends Actor {
  import PostgresItemActor._
  import com.startdown.models.ItemJsonProtocol._
  import context.dispatcher


  case class Response(success: Boolean,
                      single: Option[Item] = None,
                      multiple: Option[Seq[Item]] = None,
                      message: Option[String] = None)

  implicit val responseFormat = jsonFormat4(Response)

  def makeResponse(f: Future[Any]) =
    f.map {
      case single: Some[Item] => new Response(true, single = single)
      case multiple: Seq[Item] => new Response(true, multiple = Some(multiple))
      case None => new Response(false)
      case _ => new Response(true)
    }.recover { case cause => new Response(false, message = Some(cause
        .toString)) }
        .map { case r => r.toJson.compactPrint }


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
