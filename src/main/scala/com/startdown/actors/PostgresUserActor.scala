package com.startdown.actors

import akka.actor.Actor
import akka.pattern.pipe
import com.startdown.models.{User, UserDao}
import com.startdown.utils.CRUD
import spray.json._

import scala.concurrent.Future

/**
  * infm created it with love on 11/7/15. Enjoy ;)
  */

object PostgresUserActor extends CRUD[User, String] {}

class PostgresUserActor extends Actor {

  import PostgresUserActor._
  import com.startdown.models.UserJsonProtocol._
  import context.dispatcher

  case class Response(success: Boolean,
                      single: Option[User] = None,
                      multiple: Option[Seq[User]] = None,
                      message: Option[String] = None)

  implicit val responseFormat = jsonFormat4(Response)

  def makeResponse(f: Future[Any]) =
    f.map {
      case single: Some[User] => new Response(true, single = single)
      case multiple: Seq[User] => new Response(true, multiple = Some(multiple))
      case None => new Response(false)
      case _ => new Response(true)
    }.recover { case cause => new Response(false, message = Some(cause
      .toString)) }
      .map { case r => r.toJson.compactPrint }

  override def receive = {
    case FetchAll =>
      makeResponse(UserDao.listAllUsers) pipeTo sender

    case Create(u: User) =>
      makeResponse(UserDao.addUser(u)) pipeTo sender

    case Read(username: String) =>
      makeResponse(UserDao.findUser(username)) pipeTo sender

    case Update(u: User) =>
      makeResponse(UserDao.updateUser(u)) pipeTo sender

    case Delete(username: String) =>
      makeResponse(UserDao.deleteUser(username)) pipeTo sender

    case DeleteAll =>
      makeResponse(UserDao.deleteAll) pipeTo sender

    case CreateTable =>
      makeResponse(UserDao.createTable.map(_.toJson.compactPrint)) pipeTo sender

    case DropTable =>
      makeResponse(UserDao.dropTable.map(_.toJson.compactPrint)) pipeTo sender
  }
}
