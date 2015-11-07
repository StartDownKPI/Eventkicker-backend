package com.startdown.utils

import com.startdown.models.UserDao
import slick.driver.PostgresDriver.api._
import slick.jdbc.meta.MTable

/**
  * infm created it with love on 11/7/15. Enjoy ;)
  */
trait PostgresSupport {
  // TODO: Replace with config

  protected implicit def executor =
    scala.concurrent.ExecutionContext.Implicits.global

  def db = Database.forConfig("postgres")

  def startPostgres = {
    db.run(MTable.getTables) map {
      case v =>
        val table = v.find(_.name.name == "users")
        if (table.isEmpty)
          UserDao.createTable
    }
  }
}
