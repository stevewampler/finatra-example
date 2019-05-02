package com.sgw.example.modules

import com.google.inject.{Provides, Singleton}
import com.sgw.example.IOExecutionContext
import com.sgw.example.services.{FooService, PingService}
import com.sgw.example.web.controllers.graphql.GraphQLController
import com.sgw.example.web.controllers.rest.HelloWorldController
import com.twitter.finatra.json.FinatraObjectMapper
import com.twitter.inject.TwitterModule

import scala.concurrent.ExecutionContext

object WebModule extends TwitterModule {

  @Singleton
  @Provides
  def helloWorldController(): HelloWorldController = HelloWorldController()

  @Singleton
  @Provides
  def pingService(): PingService = PingService()

  @Singleton
  @Provides
  def fooService(): FooService = FooService()

  @Singleton
  @Provides
  def gqlController(
    pingService: PingService,
    fooService: FooService,
    objectMapper: FinatraObjectMapper,
    @IOExecutionContext ioExecutionContext: ExecutionContext
  ): GraphQLController = GraphQLController(
    pingService,
    fooService,
    objectMapper
  )(ioExecutionContext)
}
