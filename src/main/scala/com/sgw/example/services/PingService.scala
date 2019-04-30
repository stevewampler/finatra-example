package com.sgw.example.services

import com.sgw.example.web.controllers.ExampleContext
import com.twitter.util.Future
import sangria.schema._
import com.sgw.example.utils.ExtendedTwitterFuture._

class PingService {
  def ping(): Future[String] = Future.value("pong")
}

object PingService {

  object Schema {
    // in a broswer: http://localhost:8888/graphql?query={Ping{ping}}
    val pingQueries = ObjectType("Ping", fields[ExampleContext, PingService](
      Field(
        name = "ping",
        fieldType = StringType,
        description = Some("Ping Server"),
        resolve = ctx => ctx.ctx.pingService.ping().toScalaFuture
      )
    ))
  }
}
