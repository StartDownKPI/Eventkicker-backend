package com.startdown.models

import com.startdown.utils.PostgresSupport
import slick.driver.PostgresDriver.api._
import slick.lifted.{TableQuery, Tag}
import spray.json.DefaultJsonProtocol

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * infm created it with love on 11/7/15. Enjoy ;)
  */

case class User(username: String, name: String,
                password: String, balance: Int)

object UserJsonProtocol extends DefaultJsonProtocol {
  implicit val userFormat = jsonFormat4(User)
}

/**
I am super coder and know this
      John Stead Fedorovich
    Ich bin super koder und weiss noch
  */
object UserDao extends PostgresSupport {

  class Users(tag: Tag) extends Table[(String, String, String, Int)](tag,
    "users") {
    def username = column[String]("USERNAME", O.PrimaryKey)
    def name = column[String]("NAME")
    def password = column[String]("PASSWD")
    def balance = column[Int]("BALANCE")

    def * = (username, name, password, balance)
  }

  val users = TableQuery[Users]
  val createTable = users.schema.create
  val dropTable = users.schema.drop

  def listAllUsers =
    try {
      Await.result(db.run(users.result map { case seq =>
        seq map { case t =>
          (User.apply _).tupled(t)
        }
      }), Duration.Inf)
    } finally db.close
}