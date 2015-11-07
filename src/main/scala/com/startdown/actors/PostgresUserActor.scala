package com.startdown.actors

import akka.actor.Actor
import com.startdown.models.{User, UserDao}
import com.startdown.utils.CRUD
import spray.json._

/**
  * infm created it with love on 11/7/15. Enjoy ;)
  */

object PostgresUserActor extends CRUD[User, String] {}

class PostgresUserActor extends Actor {
  import PostgresUserActor._
  import com.startdown.models.UserJsonProtocol._

  override def receive = {
    case FetchAll =>
      sender ! UserDao.listAllUsers.toJson.compactPrint
    case Create(u: User) =>
      sender ! UserDao.addUser(u).toJson.compactPrint
    case Read(username: String) =>
      sender ! UserDao.findUser(username).toJson.compactPrint
    case Update(u: User) =>
      sender ! UserDao.updateUser(u).toJson.compactPrint
    case Delete(username: String) =>
      sender ! UserDao.deleteUser(username).toJson.compactPrint
    case DeleteAll =>
      sender ! UserDao.deleteAll.toJson.compactPrint
    case CreateTable =>
      sender ! UserDao.createTable.toJson.compactPrint
    case DropTable =>
      sender ! UserDao.dropTable.toJson.compactPrint
  }
}
