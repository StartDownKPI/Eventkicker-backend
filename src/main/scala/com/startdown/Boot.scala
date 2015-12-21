package com.startdown

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import com.startdown.actors.MainActor
import com.startdown.utils.PostgresSupport
import spray.can.Http

import scala.concurrent.duration._

/**
  * infm created it with love on 11/7/15. Enjoy ;)
  */
object Boot extends App with PostgresSupport {

  implicit val system = ActorSystem("on-spray-can")
  val service = system.actorOf(Props[MainActor], "main-actor")

  startPostgres

  implicit val timeout = Timeout(120.seconds)
  IO(Http) ? Http.Bind(service, interface = "0.0.0.0", port = 3600)
}
