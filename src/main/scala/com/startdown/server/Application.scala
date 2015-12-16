package com.startdown.server

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import com.startdown.actors.MainActor
import com.startdown.utils.PostgresSupport
import spray.can.Http

/**
  * infm created it with love on 12/16/15. Enjoy ;)
  */
class Application() extends ApplicationLifecycle with PostgresSupport {

  private[this] var started: Boolean = false
  implicit val actorSystem = ActorSystem("on-spray-can")

  def start() {
    if (!started) {
      startPostgres
      started = true

      val service = actorSystem.actorOf(Props[MainActor], "main-actor")
      IO(Http) ? Http.Bind(service, interface = "localhost", port = 3600)
    }
  }

  def stop() {
    if (started) {
      started = false
      actorSystem.shutdown()
    }
  }

}
