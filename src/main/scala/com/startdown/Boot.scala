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

  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("on-spray-can")

  // create and start our service actor
  val service = system.actorOf(Props[MainActor], "main-actor")

  startPostgres

  implicit val timeout = Timeout(5.seconds)
  // start a new HTTP server on port 8080 with our service actor as the handler
  IO(Http) ? Http.Bind(service, interface = "localhost", port = 3600)
}
