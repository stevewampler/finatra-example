package com.sgw.example.web.controllers

import com.sgw.example.services.PingService
import com.twitter.finagle.http.exp.Multipart.FileUpload

case class ExampleContext(
  pingService: PingService,
  fileParts: Map[String, Seq[FileUpload]]
)
