package com.sgw.example.web.controllers.graphql

import com.sgw.example.services.Bar
import com.twitter.util.Future
import sangria.execution.deferred.{Fetcher, HasId}
import sangria.macros.derive.{GraphQLDescription, deriveObjectType}
import sangria.schema.ObjectType
import com.sgw.example.utils.ExtendedTwitterFuture._

object GraphQLBar {
  implicit val gqlType: ObjectType[Unit, GraphQLBar] = deriveObjectType[Unit, GraphQLBar]()

  def apply(bar: Bar): GraphQLBar = GraphQLBar(
    id = bar.id,
    name = bar.name
  )

  case class DeferredInput(foo: GraphQLFoo)
  case class DeferredResult(input: DeferredInput, bar: Option[GraphQLBar])

  val gqlDeferredFetcher: Fetcher[ExampleGraphQLContext, DeferredResult, DeferredResult, DeferredInput] = Fetcher {
    (
      _: ExampleGraphQLContext,
      inputs: Seq[DeferredInput]
    ) => {
      // here's where we'd actually hit the DB to get each foo's bar
      Future.value[Seq[DeferredResult]] {
        inputs.map { input =>
          DeferredResult(input, input.foo.bar)
        }
      }.toScalaFuture
    }
  }(HasId(_.input))
}

case class GraphQLBar(
  @GraphQLDescription("This Bar's id.")
  id: Long,
  @GraphQLDescription("This Bar's name.")
  name: String,
)
