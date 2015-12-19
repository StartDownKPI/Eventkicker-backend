package com.startdown.server

import akka.actor.Props
import akka.pattern.ask
import com.startdown.actors._
import com.startdown.models._

/**
  * infm created it with love on 11/7/15. Enjoy ;)
  */

trait MainService extends WebService {

  import com.startdown.models.UserJsonProtocol._
  import com.startdown.models.EventJsonProtocol._
  import com.startdown.models.ItemJsonProtocol._
  import com.startdown.models.CommentJsonProtocol._
  import com.startdown.models.LikeJsonProtocol._
  import com.startdown.models.HelpRequestJsonProtocol._
  import spray.httpx.SprayJsonSupport._

  val postgresUserWorker = actorRefFactory.actorOf(Props[PostgresUserActor],
    "postgres-user-worker")

  def postgresUserCall(message: Any) =
    (postgresUserWorker ? message).mapTo[String].map(identity)

  val postgresEventWorker = actorRefFactory.actorOf(Props[PostgresEventActor],
    "postgres-event-worker")

  def postgresEventCall(message: Any) =
    (postgresEventWorker ? message).mapTo[String].map(identity)

  val postgresItemWorker = actorRefFactory.actorOf(Props[PostgresItemActor],
    "postgres-item-worker")

  def postgresItemCall(message: Any) =
    (postgresItemWorker ? message).mapTo[String].map(identity)

  val postgresCommentWorker = actorRefFactory.actorOf(
    Props[PostgresCommentActor], "postgres-comment-worker")

  def postgresCommentCall(message: Any) =
    (postgresCommentWorker ? message).mapTo[String].map(identity)

  val postgresLikeWorker = actorRefFactory.actorOf(Props[PostgresLikeActor],
    "postgres-like-worker")

  def postgresLikeCall(message: Any) =
    (postgresLikeWorker ? message).mapTo[String].map(identity)

  val postgresHelpRequestWorker = actorRefFactory.actorOf(
    Props[PostgresHelpRequestActor], "postgres-help-request-worker")

  def postgresHelpRequestCall(message: Any) =
    (postgresHelpRequestWorker ? message).mapTo[String].map(identity)

  val userServiceRoutes = {
    import PostgresUserActor._
    pathPrefix("users") {
      pathEndOrSingleSlash {
        get {
          complete {
            postgresUserCall(FetchAll)
          }
        } ~
            post {
              entity(as[User]) { user =>
                complete {
                  postgresUserCall(Create(user))
                }
              }
            } ~
            delete {
              complete {
                postgresUserCall(DeleteAll)
              }
            }
      } ~
          path("table") {
            get {
              complete {
                postgresUserCall(CreateTable)
              }
            } ~
                delete {
                  complete {
                    postgresUserCall(DropTable)
                  }
                }
          }
    } ~
        path("user" / Segment) { username =>
          get {
            complete {
              postgresUserCall(Read(username))
            }
          } ~
              put {
                entity(as[User]) { user =>
                  complete {
                    postgresUserCall(Update(user))
                  }
                }
              } ~
              delete {
                complete {
                  postgresUserCall(Delete(username))
                }
              }
        }
  }


  val eventServiceRoutes = {
    import PostgresEventActor._
    pathPrefix("events") {
      pathEndOrSingleSlash {
        get {
          complete {
            postgresEventCall(FetchAll)
          }
        } ~
            post {
              entity(as[Event]) { event =>
                complete {
                  postgresEventCall(Create(event))
                }
              }
            } ~
            delete {
              complete {
                postgresEventCall(DeleteAll)
              }
            }
      } ~
          path("table") {
            get {
              complete {
                postgresEventCall(CreateTable)
              }
            } ~
                delete {
                  complete {
                    postgresEventCall(DropTable)
                  }
                }
          }
    } ~
        path("event" / LongNumber) { eventId =>
          get {
            complete {
              postgresEventCall(Read(eventId))
            }
          } ~
              put {
                entity(as[Event]) { event =>
                  complete {
                    postgresEventCall(Update(event))
                  }
                }
              } ~
              delete {
                complete {
                  postgresEventCall(Delete(eventId))
                }
              }
        }
  }

  val itemServiceRoutes = {
    import PostgresItemActor._
    pathPrefix("items") {
      pathEndOrSingleSlash {
        get {
          complete {
            postgresItemCall(FetchAll)
          }
        } ~
            post {
              entity(as[Item]) { item =>
                complete {
                  postgresItemCall(Create(item))
                }
              }
            } ~
            delete {
              complete {
                postgresItemCall(DeleteAll)
              }
            }
      } ~
          path("table") {
            get {
              complete {
                postgresItemCall(CreateTable)
              }
            } ~
                delete {
                  complete {
                    postgresItemCall(DropTable)
                  }
                }
          }
    } ~
        path("item" / LongNumber) { itemId =>
          get {
            complete {
              postgresItemCall(Read(itemId))
            }
          } ~
              put {
                entity(as[Item]) { item =>
                  complete {
                    postgresItemCall(Update(item))
                  }
                }
              } ~
              delete {
                complete {
                  postgresItemCall(Delete(itemId))
                }
              }
        }
  }
  val commentServiceRoutes = {
    import PostgresCommentActor._
    pathPrefix("comments") {
      pathEndOrSingleSlash {
        get {
          complete {
            postgresCommentCall(FetchAll)
          }
        } ~
            post {
              entity(as[Comment]) { comment =>
                complete {
                  postgresCommentCall(Create(comment))
                }
              }
            } ~
            delete {
              complete {
                postgresCommentCall(DeleteAll)
              }
            }
      } ~
          path("table") {
            get {
              complete {
                postgresCommentCall(CreateTable)
              }
            } ~
                delete {
                  complete {
                    postgresCommentCall(DropTable)
                  }
                }
          }
    } ~
        path("comment" / LongNumber) { commentId =>
          get {
            complete {
              postgresCommentCall(Read(commentId))
            }
          } ~
              put {
                entity(as[Comment]) { comment =>
                  complete {
                    postgresCommentCall(Update(comment))
                  }
                }
              } ~
              delete {
                complete {
                  postgresCommentCall(Delete(commentId))
                }
              }
        }
  }

  val likeServiceRoutes = {
    import PostgresLikeActor._
    pathPrefix("likes") {
      pathEndOrSingleSlash {
        get {
          complete {
            postgresLikeCall(FetchAll)
          }
        } ~
            post {
              entity(as[Like]) { like =>
                complete {
                  postgresLikeCall(Create(like))
                }
              }
            } ~
            delete {
              complete {
                postgresLikeCall(DeleteAll)
              }
            }
      } ~
          path("table") {
            get {
              complete {
                postgresLikeCall(CreateTable)
              }
            } ~
                delete {
                  complete {
                    postgresLikeCall(DropTable)
                  }
                }
          }
    } ~
        path("like" / LongNumber) { likeId =>
          get {
            complete {
              postgresLikeCall(Read(likeId))
            }
          } ~
              put {
                entity(as[Like]) { like =>
                  complete {
                    postgresLikeCall(Update(like))
                  }
                }
              } ~
              delete {
                complete {
                  postgresLikeCall(Delete(likeId))
                }
              }
        }
  }

  val helpRequestServiceRoutes = {
    import PostgresHelpRequestActor._
    pathPrefix("help-requests") {
      pathEndOrSingleSlash {
        get {
          complete {
            postgresHelpRequestCall(FetchAll)
          }
        } ~
            post {
              entity(as[HelpRequest]) { helpRequest =>
                complete {
                  postgresHelpRequestCall(Create(helpRequest))
                }
              }
            } ~
            delete {
              complete {
                postgresHelpRequestCall(DeleteAll)
              }
            }
      } ~
          path("table") {
            get {
              complete {
                postgresHelpRequestCall(CreateTable)
              }
            } ~
                delete {
                  complete {
                    postgresHelpRequestCall(DropTable)
                  }
                }
          }
    } ~
        path("help-request" / LongNumber) { helpRequestId =>
          get {
            complete {
              postgresHelpRequestCall(Read(helpRequestId))
            }
          } ~
              put {
                entity(as[HelpRequest]) { helpRequest =>
                  complete {
                    postgresHelpRequestCall(Update(helpRequest))
                  }
                }
              } ~
              delete {
                complete {
                  postgresHelpRequestCall(Delete(helpRequestId))
                }
              }
        } ~ path("help-request" / "submit") {
      post {
        entity(as[HelpRequest]) { helpRequest =>
          complete {
            postgresHelpRequestCall(Submit(helpRequest))
          }
        }
      }
    }
  }
}
