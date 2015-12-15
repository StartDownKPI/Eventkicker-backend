package com.startdown.models

import com.startdown.utils.PostgresSupport
import slick.driver.PostgresDriver.api._
import slick.lifted.Tag
import spray.json.DefaultJsonProtocol

/**
  * infm created it with love on 12/14/15. Enjoy ;)
  * Shameless copy paste, will refactor this properly in next version.
  */

case class Item(id: Option[Long],
                name: Option[String],
                description: Option[String],
                pictureUrl: Option[String],
                eventId: Option[Long])

object ItemJsonProtocol extends DefaultJsonProtocol {
  implicit val itemFormat = jsonFormat5(Item)
}

object ItemDao extends PostgresSupport {
  class Items(tag: Tag) extends Table[Item](tag, "items") {
    def id = column[Long]("id", O.AutoInc, O.PrimaryKey)
    def name = column[String]("name")
    def description = column[Option[String]]("description")
    def pictureUrl = column[Option[String]]("pictureUrl")
    def eventId = column[Long]("eventId")
    
    def * = (id.?, name.?, description, pictureUrl, eventId.?) <> (Item.tupled,
        Item.unapply)

    def event = foreignKey("eventFk", eventId, EventDao.events)(_.id, onUpdate =
        ForeignKeyAction.Restrict, onDelete = ForeignKeyAction.Cascade)
  }
  
  val items = TableQuery[Items]

  def createTable =
    db.run(items.schema.create)

  def dropTable =
    db.run(items.schema.drop)

  def listAllItems =
    db.run(items.result)

  def addItem(i: Item) =
    db.run(items += i)

  def findItem(id: Long) = {
    db.run(items.filter(_.id === id).result
        map {
      case Seq(x, _*) => Some(x)
      case _ => None
    })
  }

  def getUpdatableColumns(is: Items) =
    (is.name, is.description, is.pictureUrl, is.eventId)
  def getUpdatableValues(i: Item) =
    (i.name.get, i.description, i.pictureUrl, i.eventId.get)

  def updateItem(i: Item) = {
    val ensure = i.id.get
    val columns = for {
      is <- items.filter(_.id === ensure)
    } yield getUpdatableColumns(is)
    db.run(columns.update(getUpdatableValues(i)))
  }

  def deleteItem(id: Long) = {
    val filterQ = items.filter(_.id === id)
    db.run(filterQ.result zip filterQ.delete map { case (res, _) =>
      res match {
        case Seq(x, _*) => Some(x)
        case _ => None
      }
    })
  }

  def deleteAll =
    db.run(items.delete)
}
