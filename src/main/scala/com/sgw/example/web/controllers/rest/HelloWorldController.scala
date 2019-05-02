package com.sgw.example.web.controllers.rest

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.util.logging.Logging

case class HelloWorldController() extends Controller with Logging {
  get("/hello") {
    request: Request => response.ok("Hello, World")
  }
}
