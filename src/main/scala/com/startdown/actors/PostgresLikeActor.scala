package com.startdown.actors

import akka.actor.Actor
import akka.pattern.pipe
import com.startdown.models.{Like, LikeDao}
import com.startdown.utils.{CRUD, Response, Responsive}
import spray.json._

/**
  * infm created it with love on 12/16/15. Enjoy ;)
  */
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