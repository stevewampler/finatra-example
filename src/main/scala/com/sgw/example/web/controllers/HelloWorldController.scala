package com.sgw.example.web.controllers

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.util.logging.Logging

class HelloWorldController extends Controller with Logging {
  get("/hello") {
    request: Request => response.ok("Hello, World")
  }
}
