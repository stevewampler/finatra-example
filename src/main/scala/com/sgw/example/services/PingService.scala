package com.sgw.example.services

import com.twitter.util.Future

case class PingService() {
  def ping(): Future[String] = Future.value("pong")
}
