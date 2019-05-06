package com.sgw.example.web.controllers.graphql

import com.sgw.example.services.Bar
import sangria.execution.deferred.{Fetcher, HasId}
import sangria.macros.derive.{GraphQLDescription, deriveObjectType}
import sangria.schema.ObjectType

object GraphQLBar {
  implicit val gqlType: ObjectType[Unit, GraphQLBar] = deriveObjectType[Unit, GraphQLBar]()

  def apply(bar: Bar): GraphQLBar = GraphQLBar(
    id = bar.id,
    name = bar.name
  )

  case class DeferredInput()
  case class DeferredResult(input: DeferredInput, bars: Seq[GraphQLBar])

  val gqlDeferredFetcher: Fetcher[ExampleGraphQLContext, DeferredResult, DeferredResult, DeferredInput] = Fetcher { (ctx: ExampleGraphQLContext, inputs: Seq[DeferredInput]) =>
//    val deferredInputsByCompanyId = inputs.
//      groupBy { deferredInput =>
//        deferredInput.directoryCompanyId
//      }
//
//    val userIdsByCompanyId = deferredInputsByCompanyId.map { case (directoryCompanyId, deferredInputs) =>
//      directoryCompanyId -> deferredInputs.flatMap { deferredInput =>
//        deferredInput.directoryUserIds
//      }
//    }
//
//    val futures = userIdsByCompanyId.map { case (directoryCompanyId, directoryUserIds) =>
//      ctx.coreServicesClient.localConvUsersInfoForDirectoryIds(
//        directoryCompanyId,
//        directoryUserIds.toSet
//      ).map { users =>
//        directoryCompanyId -> users.filter { user =>
//          // throw out users that don't have a directory user id (should never happen, but we have to check b/c
//          // directoryUserId is optional :(
//          user.directoryUserId.isDefined
//        }.groupBy { user =>
//          user.directoryUserId.get
//        }.mapValues { users =>
//          // there should zero or one user for each directory-user id
//          users.headOption
//        }
//      }
//    }.toSeq
//
//    TwitterFuture.collect {
//      futures
//    }.map { maybeUserByCompanyIdAndUserId =>
//      maybeUserByCompanyIdAndUserId.toMap
//    }.map { maybeUserByCompanyIdAndUserId =>
//      // for each company ...
//      deferredInputsByCompanyId.flatMap { case (companyId, deferredInputs) =>
//        // for each of the company's DeferredInputs ...
//        deferredInputs.map { deferredInput =>
//          // create a DeferredResult
//          DeferredResult(
//            // containing the deferred input and ...
//            deferredInput,
//            // the DeferredInput's requested users
//            deferredInput.directoryUserIds.flatMap { userId =>
//              maybeUserByCompanyIdAndUserId(companyId)(userId)
//            }.map { user =>
//              GraphQLUser(user)
//            }
//          )
//        }
//      }.toSeq
//    }.toScalaFuture

  }(HasId(_.input))
}

case class GraphQLBar(
  @GraphQLDescription("This Bar's id.")
  id: Long,
  @GraphQLDescription("This Bar's name.")
  name: String,
)
