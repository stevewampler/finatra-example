package com.sgw.example.modules

import com.google.inject.{Provides, Singleton}
import com.sgw.example.web.controllers.HelloWorldController
import com.twitter.inject.TwitterModule

object WebModule extends TwitterModule {

  @Singleton
  @Provides
  def helloWorldController(): HelloWorldController = new HelloWorldController

//  @Singleton
//  @Provides
//  def gqlController(pingService: PingService,
//    deployEnvironment: DeployEnvironment,
//    objectMapper: FinatraObjectMapper,
//    @IOExecutionContext ioExecutionContext: ExecutionContext): GraphQLController =
//    new GraphQLController(pingService, deployEnvironment, objectMapper)(ioExecutionContext)
}
