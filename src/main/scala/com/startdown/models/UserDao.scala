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
                password: Option[String],
                balance: Option[Int])

object UserJsonProtocol extends DefaultJsonProtocol {
  implicit val userFormat = jsonFormat5(User)
}

/**
I am super coder and know this
      John Stead Fedorovich
    Ich bin super koder und weiss noch
  */
object UserDao extends PostgresSupport {

  class Users(tag: Tag) extends Table[User](tag, "users") {
    def id = column[Long]("id", O.AutoInc)
    def username = column[String]("username", O.PrimaryKey)
    def name = column[Option[String]]("name")
    def password = column[String]("passwd")
    def balance = column[Option[Int]]("balance")

    def * = (id.?, username, name, password.?, balance) <>
      (User.tupled, User.unapply)
  }

  def BCrypted(u: User) =
    u.copy(password = Some(BCrypt.hashpw(u.password.getOrElse("qwerty"),
      BCrypt.gensalt())))

  val users = TableQuery[Users]

  def createTable =
    db.run(users.schema.create)

  def dropTable =
    db.run(users.schema.drop)

  def listAllUsers =
    db.run(users.result)

  def addUser(u: User) =
    db.run(users += BCrypted(u))

  def findUser(username: String) = {
    db.run(users.filter(_.username === username).result
      map {
      case Seq(x, _*) => Some(x)
      case _ => None
    })
  }

  def getUpdatableColumns(us: Users) =
    (us.name, us.password, us.balance)
  def getUpdatableValues(u: User) =
    (u.name, u.password.get, u.balance)

  def updateUser(u: User) = {
    val columns = for {
      us <- users.filter(_.username === u.username)
    } yield getUpdatableColumns(us)
    db.run(columns.update(getUpdatableValues(BCrypted(u))))
  }

  def deleteUser(username: String) = {
    val filterQ = users.filter(_.username === username)
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