package com.sgw.example.web.controllers.graphql

import com.sgw.example.services.{Foo, FooService}
import com.sgw.example.utils.ExtendedTwitterFuture._
import play.api.libs.json.{Format, Json}
import sangria.macros.derive.{GraphQLDescription, GraphQLName, deriveInputObjectType, deriveObjectType}
import sangria.schema._
import sangria.marshalling.playJson._

object FooSchema {

  val queries = ObjectType(
    "FooQueries",
    fields[ExampleGraphQLContext, FooService](
      // http://localhost:8888/graphql?query={Foo{ping}}
      // curl -XPOST http://localhost:8888/graphql -H 'Content-Type: application/json' -d '{"query": "{ Foo { ping } }"}'
      Field(
        name = "ping",
        fieldType = StringType,
        description = Some("Foo Server"),
        resolve = ctx => ctx.ctx.fooService.ping().toScalaFuture
      )
    )
  )

  object GraphQLFoo {
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

  case class CreateFooInput(
    @GraphQLDescription("The new Foo's id.")
    id: Long,
    @GraphQLDescription("The new Foo's name.")
    name: String
  )

  implicit val graphQLFooType: ObjectType[Unit, GraphQLFoo] = deriveObjectType[Unit, GraphQLFoo]()

  implicit val graphQLCreateFooFormat: Format[CreateFooInput] = Json.format[CreateFooInput]
  implicit val graphQLCreateFooInputType: InputObjectType[CreateFooInput] = deriveInputObjectType[CreateFooInput]()
  implicit val graphQLCreateFooTypeArgument: Argument[CreateFooInput] = Argument("input", graphQLCreateFooInputType)

  val mutations = ObjectType("FooMutations",
    fields[ExampleGraphQLContext, FooService](
      // curl -XPOST http://localhost:8888/graphql -H 'Content-Type: application/json' -d '{ "query": "mutation ($input: CreateFooInput!) { Foo { createFoo(input: $input) { id name } } }", "variables": "{ \"input\": { \"id\": 1, \"name\": \"foo1\" } }" }'
      Field(
        name = "createFoo",
        fieldType = graphQLFooType,
        description = Some("Creates a new Foo"),
        arguments = graphQLCreateFooTypeArgument :: Nil,
        resolve = ctx => ctx.ctx.fooService.createFoo(ctx.arg(graphQLCreateFooTypeArgument)).map { result =>
          GraphQLFoo(result)
        }.toScalaFuture
      )
    )
  )
}
