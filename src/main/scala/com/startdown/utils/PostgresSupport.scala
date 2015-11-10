package com.startdown.utils

import com.github.tminglei.slickpg._
import com.startdown.models.UserDao
import slick.driver.PostgresDriver
import slick.driver.PostgresDriver.api._
import slick.jdbc.meta.MTable

/**
  * infm created it with love on 11/7/15. Enjoy ;)
  */
trait PostgresSupport {
  protected implicit def executor =
    scala.concurrent.ExecutionContext.Implicits.global

  def db = SinglePool.db

  def startPostgres = {
    db.run(MTable.getTables) map {
      case v =>
        val table = v.find(_.name.name == "users")
        if (table.isEmpty)
          UserDao.createTable
    }
  }
}

object SinglePool {
  lazy val db = Database.forConfig("postgres")
}

trait CustomPostgresDriver extends PostgresDriver
    with PgArraySupport
    with PgHStoreSupport {
  override val api = MyAPI
  object MyAPI extends API
      with ArrayImplicits
      with HStoreImplicits {
    implicit val strListTypeMapper =
      new SimpleArrayJdbcType[String]("text").to(_.toList)
  }
}

object CustomPostgresDriver extends CustomPostgresDriver