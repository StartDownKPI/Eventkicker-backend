package com.startdown.actors

import akka.actor.Actor
import com.startdown.server.MainService

/**
  * infm created it with love on 11/7/15. Enjoy ;)
  */
class MainActor extends Actor with MainService {
  def actorRefFactory = context

  def receive = runRoute(
    pathPrefix("api") {
      userServiceRoutes ~ eventServiceRoutes
    }
  )
}
