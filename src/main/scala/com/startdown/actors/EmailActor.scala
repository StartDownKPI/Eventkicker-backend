package com.startdown.actors

import akka.actor.Actor
import akka.pattern.pipe

/**
  * infm created it with love on 12/19/15. Enjoy ;)
  */
object EmailActor {
  case class Send(address: String, subject: String, content: String)
}

class EmailActor extends Actor {
  import EmailActor._

  override def receive = {
    case Send(address, subject, content) =>
      import courier._, Defaults._
      import com.startdown.utils.Credentials._
      val mailer = Mailer("smtp.gmail.com", 587)
          .debug(true)
          .auth(true)
          .as(adminEmailAddress, adminEmailPass)
          .startTtls(true)()

      val (user, host) = address.split("@") match {
        case Array(user: String, host: String) => (user, host)
        case _ => null
      }
      mailer(Envelope.from(adminEmailUser `@` adminEmailHost)
          .to(user `@` host)
          .subject(subject)
          .content(Text(content))) map {
        case _ => true
      } recover {
        case _ => false
      } pipeTo sender
  }
}
