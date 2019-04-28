package com.sgw.example

import com.google.inject.Module
import com.sgw.example.modules.WebModule
import com.sgw.example.web.controllers.HelloWorldController
import com.twitter.finagle.Http
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.filters.{CommonFilters, LoggingMDCFilter, TraceIdMDCFilter}
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.conversions.storage._

object ExampleServerMain extends ExampleServer

class ExampleServer extends HttpServer {
  override val modules: Seq[Module] = Seq(
    WebModule
  )

  override def configureHttp(router: HttpRouter): Unit = {
//    implicit val ioExecutionContext: ExecutionContext = injector.instance[ExecutionContext, IOExecutionContext]

    router
      .filter[LoggingMDCFilter[Request, Response]]
      .filter[TraceIdMDCFilter[Request, Response]]
//      .filter[CommonFilters]
//      .filter[CorsFilter, CorsFilterBean]
//      .filter[MDCFilter, MDCFilterBean]
//      .add[CorsController]
      .add(injector.instance[HelloWorldController])
  }

  override def configureHttpServer(server: Http.Server): Http.Server = {
    server.withMaxRequestSize(5.megabytes).withAdmissionControl.concurrencyLimit(
      maxConcurrentRequests = 128,
      maxWaiters = 2048
    )
  }
}
