package com.startdown.utils

import com.startdown.models.CommentWithAuthorName
import spray.json._

import scala.concurrent.{ExecutionContext, Future}

/**
  * infm created it with love on 12/16/15. Enjoy ;)
  */
case class Response[T](success: Boolean,
                       single: Option[T] = None,
                       multiple: Option[Seq[T]] = None,
                       message: Option[String] = None)

trait Responsive[T] {
  def makeResponse(f: Future[Any])(implicit writer: JsonWriter[Response[T]],
                                   executor: ExecutionContext) =
    f.map {
      case single: Some[T] => new Response[T](true, single = single)
      case multiple: Seq[T] => new Response[T](true, multiple = Some(multiple))
      case None => new Response[T](false)
      case _ => new Response[T](true)
    }.recover { case cause => new Response[T](false, message = Some(cause
        .toString))
    }.map { case r => r.toJson.compactPrint }
}

object ResponsiveComment extends Responsive[CommentWithAuthorName]