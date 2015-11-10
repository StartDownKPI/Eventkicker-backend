package com.startdown.actors

import akka.actor.Actor
import akka.pattern.pipe
import com.startdown.models.{EventDao, Event}
import com.startdown.utils.CRUD
import spray.json._

import scala.concurrent.Future

/**
  * infm created it with love on 11/8/15. Enjoy ;)
  */
object PostgresEventActor extends CRUD[Event, Long]

class PostgresEventActor extends Actor {
  import PostgresEventActor._
  import com.startdown.models.EventJsonProtocol._
  import context.dispatcher

  case class Response(success: Boolean,
                      single: Option[Event] = None,
                      multiple: Option[Seq[Event]] = None,
                      message: Option[String] = None)

  implicit val responseFormat = jsonFormat4(Response)

  def makeResponse(f: Future[Any]) =
    f.map {
      case single: Some[Event] => new Response(true, single = single)
      case multiple: Seq[Event] => new Response(true, multiple = Some(multiple))
      case None => new Response(false)
      case _ => new Response(true)
    }.recover { case cause => new Response(false, message = Some(cause
      .toString)) }
      .map { case r => r.toJson.compactPrint }

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