package com.startdown.server

import com.startdown.models.{User, UserDao}
import spray.routing.authentication.{BasicAuth, UserPass}
import spray.routing.directives.AuthMagnet

import scala.concurrent.{ExecutionContext, Future}

/**
  * infm created it with love on 12/19/15. Enjoy ;)
  */
class AuthInfo(val user: User) {
  def hasPermission(permission: String) = {
    // Code to verify whether user has the given permission
    false
  }
}

trait Authenticator {
  def basicUserAuthenticator(implicit ec: ExecutionContext): AuthMagnet[AuthInfo] = {
    def authenticator(userPass: Option[UserPass]): Future[Option[AuthInfo]] = {
      userPass match {
        case Some(up) =>
          UserDao.findUser(up.user).map {
            case Some(u) if u.passwordMatches(up.pass) => Some(new AuthInfo(u))
            case _ => None
          }
        case _ => Future(None)
      }
    }

    BasicAuth(authenticator _, realm = "Private API")
  }
}
