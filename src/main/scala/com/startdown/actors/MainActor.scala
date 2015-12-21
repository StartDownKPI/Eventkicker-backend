package com.startdown.actors

import akka.actor.Actor

/**
  * infm created it with love on 11/7/15. Enjoy ;)
  */
class MainActor extends Actor with LikeService
                              with HelpRequestService
                              with EventService
                              with UserService
                              with CommentService
                              with ItemService {
  def actorRefFactory = context

  def receive = runRoute(
    pathPrefix("api") {
      userServiceRoutes ~
          eventServiceRoutes ~
          itemServiceRoutes ~
          commentServiceRoutes ~
          likeServiceRoutes ~
          helpRequestServiceRoutes
    }
  )
}
