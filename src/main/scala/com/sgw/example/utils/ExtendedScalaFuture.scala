package com.sgw.example.utils

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}
import scala.concurrent.{Future => ScalaFuture}
import com.twitter.util.{Future => TwitterFuture}

object ExtendedScalaFuture {
  implicit def toExtendedScalaFuture[T](scalaFuture: ScalaFuture[T])(implicit executionContext : ExecutionContext): ExtendedScalaFuture[T] = ExtendedScalaFuture(scalaFuture)

  def toTwitterFuture[T](scalaFuture: ScalaFuture[T])(implicit executionContext : ExecutionContext): TwitterFuture[T] = {
    import com.twitter.util.{Promise => TwitterPromise}

    val twitterPromise = new TwitterPromise[T]()

    scalaFuture.onComplete {
      case Success(value) => twitterPromise.setValue(value)
      case Failure(exception) => twitterPromise.setException(exception)
    }

    twitterPromise
  }
}

case class ExtendedScalaFuture[T](scalaFuture: ScalaFuture[T])(implicit executionContext : ExecutionContext) {
  def toTwitterFuture: TwitterFuture[T] = ExtendedScalaFuture.toTwitterFuture[T](scalaFuture)
}
