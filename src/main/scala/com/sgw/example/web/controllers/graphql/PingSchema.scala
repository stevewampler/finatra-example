package com.sgw.example.web.controllers.graphql

import com.sgw.example.services.PingService
import sangria.schema._
import com.sgw.example.utils.ExtendedTwitterFuture._

object PingSchema {
  // in a browser: http://localhost:8888/graphql?query={Ping{ping}}
  val queries: ObjectType[ExampleGraphQLContext, PingService] = ObjectType("Ping", fields[ExampleGraphQLContext, PingService](
    Field(
      name = "ping",
      fieldType = StringType,
      description = Some("Ping Server"),
      resolve = ctx => ctx.ctx.pingService.ping().toScalaFuture
    )
  ))
}
