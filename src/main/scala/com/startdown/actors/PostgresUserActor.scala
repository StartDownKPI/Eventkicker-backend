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
  // TODO: Search by all non-None field of User
  case class  ReadUser(username: String)
  case class  UpdateUser(u: User)
  case class  DeleteUser(username: String)
  case object DeleteAll
  case object GetCount
  case object CreateTable
  case object DropTable
}

class PostgresUserActor extends Actor {
  import com.startdown.models.UserJsonProtocol._
  import PostgresUserActor._

  // implicit val userFormat = jsonFormat4(User)
  override def receive = {
    case FetchAll =>
      sender ! UserDao.listAllUsers.toJson.compactPrint
    case CreateUser(u: User) =>
      sender ! UserDao.addUser(u).toJson.compactPrint
    case ReadUser(username: String) =>
      sender ! UserDao.findUser(username).toJson.compactPrint
    case UpdateUser(u: User) =>
      sender ! UserDao.updateUser(u).toJson.compactPrint
    case DeleteUser(username: String) =>
      sender ! UserDao.deleteUser(username).toJson.compactPrint
  }
}
