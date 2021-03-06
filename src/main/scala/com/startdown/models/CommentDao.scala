package com.startdown.models

import com.github.tototoshi.slick.PostgresJodaSupport._
import com.startdown.models.EventDao._
import com.startdown.models.UserDao._
import com.startdown.utils.{CustomPostgresDriver, PostgresSupport}
import org.joda.time.DateTime
import spray.json._

/**
  * infm created it with love on 12/16/15. Enjoy ;)
  */

case class Comment(id: Option[Long],
                   timeCreated: Option[DateTime],
                   content: Option[String],
                   authorId: Option[Long],
                   eventId: Option[Long])

case class CommentWithAuthorName(id: Option[Long],
                                 timeCreated: Option[DateTime],
                                 content: Option[String],
                                 authorId: Option[Long],
                                 authorName: Option[String],
                                 eventId: Option[Long])

object CommentJsonProtocol extends DefaultJsonProtocol {
  import com.startdown.utils.JodaTimeJsonProtocol._
  implicit val commentFormat = jsonFormat5(Comment)
  implicit val commentWithAuthorNameFormat = jsonFormat6(CommentWithAuthorName)
}

object CommentDao extends PostgresSupport {

  import CustomPostgresDriver.api._
  class Comments(tag: Tag) extends Table[Comment](tag, "comments") {
    def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
    def timeCreated = column[DateTime]("timeCreated")
    def content = column[String]("content")
    def authorId = column[Long]("authorId")
    def eventId = column[Long]("eventId")

    def * = (id.?, timeCreated.?, content.?, authorId.?, eventId.?) <>
        (Comment.tupled, Comment.unapply)

    def author = foreignKey("authorFk", authorId,
      UserDao.users)(_.id, onUpdate = ForeignKeyAction.Restrict,
      onDelete = ForeignKeyAction.Cascade)
    def event = foreignKey("eventFk", eventId, EventDao.events)(_.id, onUpdate =
        ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
  }

  val comments = TableQuery[Comments]

  def createTable =
    db.run(comments.schema.create)

  def dropTable =
    db.run(comments.schema.drop)

  def listAllComments =
    db.run(comments.result)

  def addComment(c: Comment) =
    db.run(comments += c.copy(timeCreated = Some(DateTime.now())))

  def findComment(id: Long) = {
    db.run(comments.filter(_.id === id).result
        map {
      case Seq(x, _*) => Some(x)
      case _ => None
    })
  }

  def getUpdatableColumns(cs: Comments) =
    (cs.timeCreated, cs.content, cs.authorId, cs.eventId)
  def getUpdatableValues(c: Comment) =
    (c.timeCreated.orNull, c.content.orNull, c.authorId.get,
        c.eventId.get)

  def updateComment(c: Comment) = {
    val ensure = c.id.get
    val columns = for {
      cs <- comments.filter(_.id === ensure)
    } yield getUpdatableColumns(cs)
    db.run(columns.update(getUpdatableValues(c)))
  }

  def deleteComment(id: Long) = {
    val filterQ = comments.filter(_.id === id)
    db.run(filterQ.result zip filterQ.delete map { case (res, _) =>
      res match {
        case Seq(x, _*) => Some(x)
        case _ => None
      }
    })
  }

  def deleteAll =
    db.run(comments.delete)

  def getForEvent(eventId: Long, limit: Long) =
    db.run {
      val joined = for {
        ((c, u), e) <- comments join users on (_.authorId === _.id) join
            events on (_._1.eventId === _.id)
      } yield (c, e.id, u.name)
      val j =
        if (limit > 0) joined.sortBy(_._1.timeCreated.desc)
        else joined
      val filtered = j.result.map(r => r.filter(t => t._2 == eventId)
          .map {
            case (c, _, authorName) =>
              CommentWithAuthorName(c.id, c.timeCreated, c.content, c.authorId,
                authorName, c.eventId)
          })
      val result =
        if (limit > 0) filtered.map{ r => r.take(limit.asInstanceOf[Int]) }
        else filtered

      result
    }
}
