package com.sgw.example.modules

import com.google.inject.{Provides, Singleton}
import com.sgw.example.IOExecutionContext
import com.sgw.example.services.PingService
import com.sgw.example.web.controllers.{GraphQLController, HelloWorldController}
import com.twitter.finatra.json.FinatraObjectMapper
import com.twitter.inject.TwitterModule

import scala.concurrent.ExecutionContext

object WebModule extends TwitterModule {

  @Singleton
  @Provides
  def helloWorldController(): HelloWorldController = new HelloWorldController

  @Singleton
  @Provides
  def pingService(): PingService = new PingService()

  @Singleton
  @Provides
  def gqlController(
    pingService: PingService,
    objectMapper: FinatraObjectMapper,
    @IOExecutionContext ioExecutionContext: ExecutionContext
  ): GraphQLController = new GraphQLController(
    pingService,
    objectMapper
  )(ioExecutionContext)
}
