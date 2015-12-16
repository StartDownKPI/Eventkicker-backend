package com.startdown.models

import com.startdown.utils.PostgresSupport
import slick.driver.PostgresDriver.api._
import slick.lifted.Tag
import spray.json.DefaultJsonProtocol

/**
  * infm created it with love on 12/16/15. Enjoy ;)
  */
case class Like(id: Option[Long],
                authorId: Option[Long],
                eventId: Option[Long])

object LikeJsonProtocol extends DefaultJsonProtocol {
  implicit val likeFormat = jsonFormat3(Like)
}

object LikeDao extends PostgresSupport {
  class Likes(tag: Tag) extends Table[Like](tag, "likes") {
    def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
    def authorId = column[Long]("authorId")
    def eventId = column[Long]("eventId")

    def * = (id.?, authorId.?, eventId.?) <> (Like.tupled, Like.unapply)

    def author = foreignKey("authorFk", authorId,
      UserDao.users)(_.id, onUpdate = ForeignKeyAction.Restrict,
      onDelete = ForeignKeyAction.Cascade)

    def event = foreignKey("eventFk", eventId, EventDao.events)(_.id, onUpdate =
        ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
  }

  val likes = TableQuery[Likes]

  def createTable =
    db.run(likes.schema.create)

  def dropTable =
    db.run(likes.schema.drop)

  def listAllLikes =
    db.run(likes.result)

  def addLike(l: Like) =
    db.run(likes += l)

  def findLike(id: Long) = {
    db.run(likes.filter(_.id === id).result
        map {
      case Seq(x, _*) => Some(x)
      case _ => None
    })
  }

  def deleteLike(id: Long) = {
    val filterQ = likes.filter(_.id === id)
    db.run(filterQ.result zip filterQ.delete map { case (res, _) =>
      res match {
        case Seq(x, _*) => Some(x)
        case _ => None
      }
    })
  }

  def deleteAll =
    db.run(likes.delete)
}
