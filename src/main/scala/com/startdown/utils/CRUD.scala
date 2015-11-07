package com.startdown.utils

/**
  * infm created it with love on 11/8/15. Enjoy ;)
  */
trait CRUD[T, PK] {
  case object FetchAll
  case class Create(x: T)
  case class Read(key: PK)
  case class Update(x: T)
  case class Delete(key: PK)
  case object DeleteAll
  case object CreateTable
  case object DropTable
}
