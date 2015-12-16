package com.startdown.utils

import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat
import spray.json._

/**
  * infm created it with love on 12/16/15. Enjoy ;)
  */
object JodaTimeJsonProtocol extends DefaultJsonProtocol {
  val formatter = ISODateTimeFormat.basicDateTimeNoMillis
  implicit object JodaTimeFormat extends RootJsonFormat[DateTime] {
    def write(obj: DateTime): JsValue =
      JsString(formatter.print(obj))
    def read(json: JsValue): DateTime = json match {
      case JsString(s) => try {
        formatter.parseDateTime(s)
      } catch {
        case t: Throwable => error(s)
      }
      case _ =>
        error(json.toString())
    }
    def error(v: Any): DateTime = {
      val example = formatter.print(0)
      deserializationError(s"'$v' is not a valid date value. Dates must " +
          s"be in compact ISO-8601 format, e.g. '$example'")
    }
  }
}


