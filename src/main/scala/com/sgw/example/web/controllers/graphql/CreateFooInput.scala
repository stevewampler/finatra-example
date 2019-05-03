package com.sgw.example.web.controllers.graphql

import play.api.libs.json.{Format, Json}
import sangria.macros.derive.{GraphQLDescription, deriveInputObjectType}
import sangria.schema.{Argument, InputObjectType}
import sangria.marshalling.playJson._

object CreateFooInput {
  implicit val format: Format[CreateFooInput] = Json.format[CreateFooInput]
  implicit val inputType: InputObjectType[CreateFooInput] = deriveInputObjectType[CreateFooInput]()
  implicit val typeArgument: Argument[CreateFooInput] = Argument("input", inputType)
}

case class CreateFooInput(
  @GraphQLDescription("The new Foo's id.")
  id: Long,
  @GraphQLDescription("The new Foo's name.")
  name: String
)
