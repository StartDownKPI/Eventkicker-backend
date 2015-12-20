package com.startdown.models

import com.github.tototoshi.slick.PostgresJodaSupport._
import com.startdown.utils.{CustomPostgresDriver, PostgresSupport}
import org.joda.time.DateTime
import spray.json._

/**
  * infm created it with love on 11/7/15. Enjoy ;)
  */

case class Event(id: Option[Long],
                 name: Option[String],
                 timeCreated: Option[DateTime],
                 timeScheduled: Option[DateTime],
                 description: Option[String],
                 pictureUrl: Option[String],
                 authorId: Option[Long])

object EventJsonProtocol extends DefaultJsonProtocol {
  import com.startdown.utils.JodaTimeJsonProtocol._
  implicit val eventFormat = jsonFormat(Event, "id", "name", "timeCreated",
    "timeScheduled", "description", "pictureUrl", "authorId")

}

object EventDao extends PostgresSupport {

  import CustomPostgresDriver.api._
  class Events(tag: Tag) extends Table[Event](tag, "events") {
    def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
    def name = column[String]("name")
    def timeCreated = column[DateTime]("timeCreated")
    def timeScheduled = column[DateTime]("timeScheduled")
    def description = column[Option[String]]("description")
    def pictureUrl = column[Option[String]]("pictureUrl")
    def authorId = column[Long]("authorId")

    def * = (id.?, name.?, timeCreated.?, timeScheduled.?, description,
      pictureUrl, authorId.?) <>
      (Event.tupled, Event.unapply)

    def author = foreignKey("authorFk", authorId,
      UserDao.users)(_.id, onUpdate = ForeignKeyAction.Restrict,
      onDelete = ForeignKeyAction.Cascade)
  }

  val events = TableQuery[Events]

  def createTable =
    db.run(events.schema.create)

  def dropTable =
    db.run(events.schema.drop)

  def listAllEvents =
    db.run(events.result)

  def addEvent(e: Event) =
    db.run(events += e.copy(timeCreated = Some(DateTime.now())))

  def findEvent(id: Long) = {
    db.run(events.filter(_.id === id).result
      map {
      case Seq(x, _*) => Some(x)
      case _ => None
    })
  }

  // Currently implemented like a shit, will add native Postgres search support.
  def searchEvents(keywords: List[String]) = {
    db.run(events.result.map(result => {
      result.filter { event =>
        keywords.map { w =>
          event.description.getOrElse("").contains(w)
        } reduceLeft ((found, acc) => found || acc)
      }
    }))
  }

  def getUpdatableColumns(es: Events) =
    (es.name, es.timeCreated, es.timeScheduled, es.description,
      es.pictureUrl, es.authorId)
  def getUpdatableValues(e: Event) =
    (e.name.orNull, e.timeCreated.orNull,
      e.timeScheduled.orNull, e.description,
      e.pictureUrl, e.authorId.get)

  def updateEvent(e: Event) = {
    val ensure = e.id.get
    val columns = for {
      es <- events.filter(_.id === ensure)
    } yield getUpdatableColumns(es)
    db.run(columns.update(getUpdatableValues(e)))
  }

  def deleteEvent(id: Long) = {
    val filterQ = events.filter(_.id === id)
    db.run(filterQ.result zip filterQ.delete map { case (res, _) =>
      res match {
        case Seq(x, _*) => Some(x)
        case _ => None
      }
    })
  }

  def deleteAll =
    db.run(events.delete)
}