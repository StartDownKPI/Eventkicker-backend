package com.startdown.server

import akka.util.Timeout
import spray.routing.HttpService
import scala.concurrent.duration._

/**
  * infm created it with love on 11/7/15. Enjoy ;)
  */
trait WebService extends HttpService {
  implicit def executionContext = actorRefFactory.dispatcher
  implicit val timeout = Timeout(240.seconds)
}
