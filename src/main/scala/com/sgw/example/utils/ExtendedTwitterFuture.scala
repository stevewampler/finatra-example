package com.sgw.example.utils

import scala.concurrent.{Future => ScalaFuture}
import com.twitter.util.{Future => TwitterFuture}

object ExtendedTwitterFuture {
  implicit def toExtendedTwitterFuture[T](twitterFuture: TwitterFuture[T]): ExtendedTwitterFuture[T] = ExtendedTwitterFuture(twitterFuture)
}

case class ExtendedTwitterFuture[T](twitterFuture: TwitterFuture[T]) {
  def toScalaFuture: ScalaFuture[T] = {
    import scala.concurrent.{Promise => ScalaPromise}
    val scalaPromise = ScalaPromise[T]
    twitterFuture.onSuccess { r: T =>
      scalaPromise.success(r)
    }
    twitterFuture.onFailure { e: Throwable =>
      scalaPromise.failure(e)
    }
    scalaPromise.future
  }
}
