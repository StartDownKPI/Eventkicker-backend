package com.startdown.models

import com.startdown.utils.PostgresSupport
import org.mindrot.jbcrypt.BCrypt
import slick.driver.PostgresDriver.api._
import slick.lifted.{TableQuery, Tag}
import spray.json.DefaultJsonProtocol

/**
  * infm created it with love on 11/7/15. Enjoy ;)
  */

case class User(id: Option[Long],
                username: String,
                name: Option[String],
                password: Option[String]) {
  def passwordMatches(pass: String): Boolean = {
    password.nonEmpty && BCrypt.checkpw(pass, password.get)
  }
}

object BCryptHelper {
  def password(pass: String) = BCrypt.hashpw(pass, BCrypt.gensalt())
  def user(u: User) =
    u.copy(password = Some(password(u.password.getOrElse("qwerty"))))
}

object UserJsonProtocol extends DefaultJsonProtocol {
  implicit val userFormat = jsonFormat4(User)
}

object UserDao extends PostgresSupport {

  class Users(tag: Tag) extends Table[User](tag, "users") {
    def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
    def username = column[String]("username")
    def name = column[Option[String]]("name")
    def password = column[String]("passwd")

    def * = (id.?, username, name, password.?) <>
      (User.tupled, User.unapply)

    def idx = index("idx_username", username, unique = true)
  }

  val users = TableQuery[Users]

  def createTable =
    db.run(users.schema.create)

  def dropTable =
    db.run(users.schema.drop)

  def listAllUsers =
    db.run(users.result)

  def addUser(u: User) =
    db.run(users += BCryptHelper.user(u))

  def findUser(username: String) = {
    db.run(users.filter(_.username === username).result
        map {
      case Seq(x, _*) => Some(x)
      case _ => None
    })
  }

  def findUser(id: Long) = {
    db.run(users.filter(_.id === id).result
        map {
      case Seq(x, _*) => Some(x)
      case _ => None
    })
  }

  def getUpdatableColumns(us: Users) =
    (us.name, us.password)
  def getUpdatableValues(u: User) =
    (u.name, u.password.get)

  def updateUser(u: User) = {
    val columns = for {
      us <- users.filter(_.username === u.username)
    } yield getUpdatableColumns(us)
    db.run(columns.update(getUpdatableValues(BCryptHelper.user(u))))
  }

  @Deprecated
  def deleteUser(username: String) = {
    val filterQ = users.filter(_.username === username)
    db.run(filterQ.result zip filterQ.delete map { case (res, _) =>
      res match {
        case Seq(x, _*) => Some(x)
        case _ => None
      }
    })
  }

  def deleteUser(userId: Long) = {
    val filterQ = users.filter(_.id === userId)
    db.run(filterQ.result zip filterQ.delete map { case (res, _) =>
      res match {
        case Seq(x, _*) => Some(x)
        case _ => None
      }
    })
  }

  def deleteAll =
    db.run(users.delete)
}