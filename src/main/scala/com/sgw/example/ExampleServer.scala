package com.sgw.example

import com.sgw.example.services.{FooService, PingService}
import com.sgw.example.web.controllers.graphql.GraphQLController
import com.sgw.example.web.controllers.rest.HelloWorldController
import com.twitter.finagle.{Http, SimpleFilter}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.{Controller, HttpServer}
import com.twitter.finatra.http.filters.{LoggingMDCFilter, TraceIdMDCFilter}
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.conversions.storage._
import com.twitter.util.StorageUnit

import scala.concurrent.ExecutionContext

object ExampleServerMain extends App {

  // TODO: use spring boot to inject these
  val ioExecutionContext = ExecutionContext.Implicits.global

  val loggingMDCFilter: SimpleFilter[Request, Response] = new LoggingMDCFilter[Request, Response]
  val traceIdMDCFilter: SimpleFilter[Request, Response] = new TraceIdMDCFilter[Request, Response]

  val filters = List(loggingMDCFilter, traceIdMDCFilter)

  val helloWorldController: Controller = HelloWorldController()

  val pingService: PingService = PingService()
  val fooService = FooService()

  val graphQLController: GraphQLController = GraphQLController(
    pingService = pingService,
    fooService = fooService
  )(
    ioExecutionContext = ioExecutionContext
  )

  val controllers = List(
    helloWorldController,
    graphQLController
  )

  val server = ExampleServer(
    filters = filters,
    controllers = controllers,
    maxRequestSize = 5.megabytes,
    maxConcurrentRequests = 128,
    maxWaiters = 2048
  )

  server.main(args)
}


case class ExampleServer(
  filters: List[SimpleFilter[Request, Response]],
  controllers: List[Controller],
  maxRequestSize: StorageUnit,
  maxConcurrentRequests: Int,
  maxWaiters: Int
) extends HttpServer {

  override def configureHttp(router: HttpRouter): Unit = {
    filters.foreach { filter =>
      router.filter(filter)
    }

    controllers.foreach { controller =>
      router.add(controller)
    }
  }

  override def configureHttpServer(
    server: Http.Server
  ): Http.Server = server.withMaxRequestSize(
    maxRequestSize
  ).withAdmissionControl.concurrencyLimit(
    maxConcurrentRequests = maxConcurrentRequests,
    maxWaiters = maxWaiters
  )
}
