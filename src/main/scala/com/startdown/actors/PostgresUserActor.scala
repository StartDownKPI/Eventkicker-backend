package com.startdown.actors

import akka.actor.Actor
import akka.pattern.pipe
import com.startdown.models.{User, UserDao}
import com.startdown.utils.CRUD
import spray.json._

import scala.concurrent.Promise

/**
  * infm created it with love on 11/7/15. Enjoy ;)
  */

object PostgresUserActor extends CRUD[User, String] {}

class PostgresUserActor extends Actor {
  import PostgresUserActor._
  import com.startdown.models.UserJsonProtocol._
  import context.dispatcher

  override def receive = {
    case FetchAll =>
      UserDao.listAllUsers.map(_.toJson.compactPrint) pipeTo sender
    case Create(u: User) =>
      UserDao.addUser(u).map(_.toJson.compactPrint) pipeTo sender
    case Read(username: String) =>
      UserDao.findUser(username).map(_.toJson.compactPrint) pipeTo sender
    case Update(u: User) =>
      UserDao.updateUser(u).map(_.toJson.compactPrint) pipeTo sender
    case Delete(username: String) =>
      UserDao.deleteUser(username).map(_.toJson.compactPrint) pipeTo sender
    case DeleteAll =>
      UserDao.deleteAll.map(_.toJson.compactPrint) pipeTo sender
    case CreateTable =>
      UserDao.createTable.map(_.toJson.compactPrint) pipeTo sender
    case DropTable =>
      UserDao.dropTable.map(_.toJson.compactPrint) pipeTo sender
  }
}
