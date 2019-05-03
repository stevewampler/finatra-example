package com.sgw.example.web.controllers.graphql

import com.sgw.example.services.Foo
import sangria.macros.derive.{GraphQLDescription, GraphQLName, deriveObjectType}
import sangria.schema.ObjectType

object GraphQLFoo {
  implicit val gqlType: ObjectType[Unit, GraphQLFoo] = deriveObjectType[Unit, GraphQLFoo]()

  def apply(foo: Foo): GraphQLFoo = GraphQLFoo(
    id = foo.id,
    name = foo.name
  )
}

@GraphQLName("Foo")
@GraphQLDescription("A Foo")
case class GraphQLFoo(
  @GraphQLDescription("This Foo's id.")
  id: Long,
  @GraphQLDescription("This Foo's name.")
  name: String
)