package com.startdown.actors

import akka.actor.Actor
import akka.pattern.pipe
import com.startdown.models.{Comment, CommentDao}
import com.startdown.utils.{CRUD, Response, Responsive}
import spray.json._

/**
  * infm created it with love on 12/16/15. Enjoy ;)
  */
object PostgresCommentActor extends CRUD[Comment, Long]

class PostgresCommentActor extends Actor with Responsive[Comment] {

  import PostgresCommentActor._
  import com.startdown.models.CommentJsonProtocol._
  import context.dispatcher

  implicit val responseFormat = jsonFormat4(Response[Comment])

  override def receive = {
    case FetchAll =>
      makeResponse(CommentDao.listAllComments) pipeTo sender

    case Create(i: Comment) =>
      makeResponse(CommentDao.addComment(i)) pipeTo sender

    case Read(id: Long) =>
      makeResponse(CommentDao.findComment(id)) pipeTo sender

    case Update(i: Comment) =>
      makeResponse(CommentDao.updateComment(i)) pipeTo sender

    case Delete(id: Long) =>
      makeResponse(CommentDao.deleteComment(id)) pipeTo sender

    case DeleteAll =>
      makeResponse(CommentDao.deleteAll) pipeTo sender

    case CreateTable =>
      makeResponse(CommentDao.createTable.map(_.toJson.compactPrint)) pipeTo sender

    case DropTable =>
      makeResponse(CommentDao.dropTable.map(_.toJson.compactPrint)) pipeTo sender
  }
}
