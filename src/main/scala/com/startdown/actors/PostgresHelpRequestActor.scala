package com.startdown.actors

import akka.actor.Actor
import akka.pattern.pipe
import com.startdown.models.{HelpRequestDao, HelpRequest}
import com.startdown.utils.{Response, Responsive, CRUD}
import spray.json._

/**
  * infm created it with love on 12/16/15. Enjoy ;)
  */
object PostgresHelpRequestActor extends CRUD[HelpRequest, Long]

class PostgresHelpRequestActor extends Actor with Responsive[HelpRequest] {
  import PostgresHelpRequestActor._
  import com.startdown.models.HelpRequestJsonProtocol._
  import context.dispatcher

  implicit val responseFormat = jsonFormat4(Response[HelpRequest])

  override def receive = {
    case FetchAll =>
      makeResponse(HelpRequestDao.listAllHelpRequests) pipeTo sender

    case Create(hr: HelpRequest) =>
      makeResponse(HelpRequestDao.addHelpRequest(hr)) pipeTo sender

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