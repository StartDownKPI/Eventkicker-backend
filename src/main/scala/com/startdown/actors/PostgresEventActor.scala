package com.startdown.actors

import akka.actor.Actor
import akka.pattern.pipe
import com.startdown.models.{EventDao, Event}
import com.startdown.utils.{Response, Responsive, CRUD}
import spray.json._

import scala.concurrent.Future

/**
  * infm created it with love on 11/8/15. Enjoy ;)
  */
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