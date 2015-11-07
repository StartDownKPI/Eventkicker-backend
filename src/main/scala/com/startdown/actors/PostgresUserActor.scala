package com.startdown.actors

import akka.actor.Actor
import com.startdown.models.{User, UserDao}
import spray.json.{DefaultJsonProtocol, _}

/**
  * infm created it with love on 11/7/15. Enjoy ;)
  */

object PostgresUserActor {
  case object FetchAll
  case class  CreateUser(u: User)
  case class  FetchUser(username: String)
  case class  ModifyTask(u: User)
  case class  DeleteTask(username: String)
  case object DeleteAll
  case object GetCount
  case object CreateTable
  case object DropTable
}

class PostgresUserActor extends Actor {
  import DefaultJsonProtocol._
  import PostgresUserActor._

  implicit val userFormat = jsonFormat4(User)
  override def receive = {
    case FetchAll =>
      sender ! UserDao.listAllUsers.toJson.compactPrint
  }
}
