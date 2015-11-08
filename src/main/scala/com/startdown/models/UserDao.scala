package com.startdown.models

import com.startdown.utils.PostgresSupport
import org.mindrot.jbcrypt.BCrypt
import slick.driver.PostgresDriver.api._
import slick.lifted.{TableQuery, Tag}
import spray.json.DefaultJsonProtocol

import scala.concurrent.Await
import scala.concurrent.duration.Duration

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
    try {
      Await.result(db.run(users.schema.create), Duration.Inf)
    } finally db.close

  def dropTable =
    try {
      Await.result(db.run(users.schema.drop), Duration.Inf)
    } finally db.close

  def listAllUsers =
    try {
      Await.result(db.run(users.result), Duration.Inf)
    } finally db.close

  def addUser(u: User) =
    try {
      Await.result(db.run(users += BCrypted(u)), Duration.Inf)
    } finally db.close

  def findUser(username: String) =
    try {
      Await.result(db.run(users.filter(_.username === username).result
        map {
        case Seq(x, _*) => Some(x)
        case _ => None
      }), Duration.Inf)
    } finally db.close

  def updateUser(u: User) =
    try {
      Await.result(db.run(users.filter(_.username === u.username).update(u)),
        Duration.Inf)
    } finally db.close

  def deleteUser(username: String) =
    try {
      val filterQ = users.filter(_.username === username)
      Await.result(
        db.run(filterQ.result zip filterQ.delete map { case (res, _) =>
          res match {
            case Seq(x, _*) => Some(x)
            case _ => None
          }
        }), Duration.Inf)
    } finally db.close

  def deleteAll =
    try {
      Await.result(db.run(users.delete).map(identity), Duration.Inf)
    } finally db.close
}