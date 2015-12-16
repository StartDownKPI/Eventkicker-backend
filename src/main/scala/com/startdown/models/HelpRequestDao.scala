package com.startdown.models

import com.github.tototoshi.slick.PostgresJodaSupport._
import com.startdown.utils.{CustomPostgresDriver, PostgresSupport}
import org.joda.time.DateTime
import spray.json.DefaultJsonProtocol

/**
  * infm created it with love on 12/16/15. Enjoy ;)
  */
case class HelpRequest(id: Option[Long],
                       timeCreated: Option[DateTime],
                       description: Option[String],
                       isAccepted: Option[Boolean],
                       authorId: Option[Long],
                       itemId: Option[Long])

object HelpRequestJsonProtocol extends DefaultJsonProtocol {
  import com.startdown.utils.JodaTimeJsonProtocol._
  implicit val helpRequestFormat = jsonFormat6(HelpRequest)
}

object HelpRequestDao extends PostgresSupport {

  import CustomPostgresDriver.api._
  class HelpRequests(tag: Tag) extends Table[HelpRequest](tag, "helpRequests") {
    def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
    def timeCreated = column[DateTime]("timeCreated")
    def description = column[Option[String]]("description")
    def isAccepted = column[Boolean]("is_accepted")
    def authorId = column[Long]("authorId")
    def itemId = column[Long]("itemId")

    def * = (id.?, timeCreated.?, description, isAccepted.?, authorId.?, 
        itemId.?) <> (HelpRequest.tupled, HelpRequest.unapply)

    def author = foreignKey("authorFk", authorId,
      UserDao.users)(_.id, onUpdate = ForeignKeyAction.Restrict,
      onDelete = ForeignKeyAction.Cascade)
    def item = foreignKey("itemFk", itemId, ItemDao.items)(_.id,
      onUpdate = ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
  }
  
  val helpRequests = TableQuery[HelpRequests]

  def createTable =
    db.run(helpRequests.schema.create)

  def dropTable =
    db.run(helpRequests.schema.drop)

  def listAllHelpRequests =
    db.run(helpRequests.result)

  def addHelpRequest(hr: HelpRequest) =
    db.run(helpRequests += hr.copy(timeCreated = Some(DateTime.now())).copy(
      isAccepted =
          if (hr.isAccepted.nonEmpty)
            hr.isAccepted
          else
            Some(false)))

  def findHelpRequest(id: Long) = {
    db.run(helpRequests.filter(_.id === id).result
        map {
      case Seq(x, _*) => Some(x)
      case _ => None
    })
  }

  def getUpdatableColumns(hrs: HelpRequests) =
    (hrs.description, hrs.isAccepted, hrs.itemId)
  def getUpdatableValues(hr: HelpRequest) =
    (hr.description, hr.isAccepted.get, hr.itemId.get)

  def updateHelpRequest(e: HelpRequest) = {
    val ensure = e.id.get
    val columns = for {
      hrs <- helpRequests.filter(_.id === ensure)
    } yield getUpdatableColumns(hrs)
    db.run(columns.update(getUpdatableValues(e)))
  }

  def deleteHelpRequest(id: Long) = {
    val filterQ = helpRequests.filter(_.id === id)
    db.run(filterQ.result zip filterQ.delete map { case (res, _) =>
      res match {
        case Seq(x, _*) => Some(x)
        case _ => None
      }
    })
  }

  def deleteAll =
    db.run(helpRequests.delete)
}
