package com.sgw.example.web.controllers.graphql

import com.sgw.example.services.{Foo, FooService}
import com.sgw.example.utils.ExtendedTwitterFuture._
import sangria.schema._

object FooSchema {

  val queries: ObjectType[ExampleGraphQLContext, FooService] = ObjectType(
    "FooQueries",
    fields[ExampleGraphQLContext, FooService](
      // http://localhost:8888/graphql?query={Foo{ping}}
      // curl -XPOST http://localhost:8888/graphql -H 'Content-Type: application/json' -d '{"query": "{ Foo { ping } }"}'
      Field(
        name = "ping",
        fieldType = StringType,
        description = Some("Pings the foo service."),
        resolve = ctx => ctx.ctx.fooService.ping().toScalaFuture
      ),
      // curl -XPOST http://localhost:8888/graphql -H 'Content-Type: application/json' -d '{"query": "{ Foo { list { id name } } }"}'
      Field(
        name = "list",
        fieldType = ListType(GraphQLFoo.gqlType),
        description = Some("Returns a list of Foos"),
        resolve = ctx => ctx.ctx.fooService.list.map { foos =>
          foos.map { foo =>
            GraphQLFoo(foo)
          }
        }.toScalaFuture
      ),
      // curl -XPOST http://localhost:8888/graphql -H 'Content-Type: application/json' -d '{"query": "{ Foo { get(id: 1) { id name } } }"}'
      Field(
        name = "get",
        fieldType = OptionType(GraphQLFoo.gqlType),
        description = Some("Returns a Foo given the Foo's id."),
        arguments = Argument("id", LongType, description = "The Foo's id.") :: Nil,
        resolve = ctx => ctx.ctx.fooService.get(ctx.args.arg[Long]("id")).map { maybeFoo =>
          maybeFoo.map { foo =>
            GraphQLFoo(foo)
          }
        }.toScalaFuture
      )
    )
  )

  val mutations: ObjectType[ExampleGraphQLContext, FooService] = ObjectType(
    "FooMutations",
    fields[ExampleGraphQLContext, FooService](
      // curl -XPOST http://localhost:8888/graphql -H 'Content-Type: application/json' -d '{ "query": "mutation ($input: CreateFooInput!) { Foo { create(input: $input) { id name } } }", "variables": "{ \"input\": { \"id\": 1, \"name\": \"foo1\" } }" }'
      Field(
        name = "create",
        fieldType = GraphQLFoo.gqlType,
        description = Some("Creates a new Foo."),
        arguments = CreateFooInput.typeArgument :: Nil,
        resolve = ctx => ctx.ctx.fooService.create(ctx.arg(CreateFooInput.typeArgument)).map { foo =>
          GraphQLFoo(foo)
        }.toScalaFuture
      ),
      // curl -XPOST http://localhost:8888/graphql -H 'Content-Type: application/json' -d '{ "query": "mutation { Foo { delete(id: 1) { id name } } }" }'
      Field(
        name = "delete",
        fieldType = OptionType(GraphQLFoo.gqlType),
        description = Some("Deletes an existing Foo by id."),
        arguments = Argument("id", LongType, description = "The Foo's id.") :: Nil,
        resolve = ctx => ctx.ctx.fooService.delete(ctx.args.arg[Long]("id")).map { maybeFoo =>
          maybeFoo.map { foo =>
            GraphQLFoo(foo)
          }
        }.toScalaFuture
      )
    )
  )
}
