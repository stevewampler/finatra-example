package com.sgw.example.web.controllers.graphql

import play.api.libs.json.{Format, Json}

object GraphQLQuery {
  implicit val format: Format[GraphQLQuery] = Json.format[GraphQLQuery]
}

case class GraphQLQuery(
  query: Option[String],
  variables: Option[String]
)
