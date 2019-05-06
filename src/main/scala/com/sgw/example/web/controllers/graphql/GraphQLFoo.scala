package com.sgw.example.web.controllers.graphql

import com.sgw.example.services.Foo
import sangria.ast.Field
import sangria.macros.derive.{GraphQLDescription, GraphQLName, ReplaceField, deriveObjectType}
import sangria.schema.{DeferredValue, ObjectType, OptionType}

object GraphQLFoo {
  implicit val gqlType: ObjectType[Unit, GraphQLFoo] = deriveObjectType[Unit, GraphQLFoo](
    ReplaceField(
      fieldName = "bar",
      field =
        Field(
          name = "bar",
          fieldType = OptionType(GraphQLBar.gqlType),
          description = Some("A foo's Bar."),
          resolve = ctx => {
            DeferredValue(
              GraphQLBar.gqlDeferredFetcher.defer(
                GraphQLBar.DeferredInput(
                  ctx.value.directoryCompanyId,
                  Seq(ctx.value.directoryUserId)
                )
              )
            ).map { deferredResult =>
              deferredResult.bars.headOption
            }
          }
        )
    ),
  )

  def apply(foo: Foo): GraphQLFoo = GraphQLFoo(
    id = foo.id,
    name = foo.name,
    bar = foo.maybeBar.map { bar =>
      GraphQLBar(bar)
    }
  )
}

@GraphQLName("Foo")
@GraphQLDescription("A Foo")
case class GraphQLFoo(
  @GraphQLDescription("This Foo's id.")
  id: Long,
  @GraphQLDescription("This Foo's name.")
  name: String,
  @GraphQLDescription("This Foo's optional Bar")
  bar: Option[GraphQLBar]
)