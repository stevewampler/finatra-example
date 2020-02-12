package com.sgw.example

import com.sgw.example.services.{FooService, PingService}
import com.sgw.example.web.controllers.graphql.GraphQLController
import com.sgw.example.web.controllers.rest.HelloWorldController
import com.twitter.finagle.{Filter, Http, SimpleFilter}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.{Controller, HttpServer}
import com.twitter.finatra.http.filters.{LoggingMDCFilter, TraceIdMDCFilter}
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.conversions.storage._
import com.twitter.finatra.json.FinatraObjectMapper

import scala.concurrent.ExecutionContext

object ExampleServerMain extends ExampleServer


class ExampleServer extends HttpServer {

  override def configureHttp(router: HttpRouter): Unit = {
    // TODO: for Spring-Boot, we would inject these into the ExampleServer via constructor args
    import ExecutionContext.Implicits.global
    val loggingMDCFilter: SimpleFilter[Request, Response] = new LoggingMDCFilter[Request, Response]
    val traceIdMDCFilter: SimpleFilter[Request, Response] = new TraceIdMDCFilter[Request, Response]
    val helloWorldController: Controller = HelloWorldController()
    val graphQLController: GraphQLController = GraphQLController(
      pingService = PingService(),
      fooService = FooService(),
      objectMapper = injector.instance[FinatraObjectMapper] // TODO: can we avoid using an objectMapper?
    )

    router
      .filter(loggingMDCFilter)
      .filter(traceIdMDCFilter)
      .add(helloWorldController)
      .add(graphQLController)
  }

  override def configureHttpServer(
    server: Http.Server
  ): Http.Server = server.withMaxRequestSize(5.megabytes).withAdmissionControl.concurrencyLimit(
    maxConcurrentRequests = 128,
    maxWaiters = 2048
  )
}
