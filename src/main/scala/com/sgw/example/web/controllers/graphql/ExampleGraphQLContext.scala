package com.sgw.example.web.controllers.graphql

import com.sgw.example.services.{FooService, PingService}
import com.twitter.finagle.http.exp.Multipart.FileUpload

case class ExampleGraphQLContext(
  pingService: PingService,
  fooService: FooService,
  fileParts: Map[String, Seq[FileUpload]]
)
