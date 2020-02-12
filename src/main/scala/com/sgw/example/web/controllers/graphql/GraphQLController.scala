package com.sgw.example.web.controllers.graphql

import java.io.InputStream
import java.nio.charset.Charset

import com.sgw.example.services.{FooService, PingService}
import com.twitter.finagle.http.exp.Multipart
import com.twitter.finagle.http.exp.Multipart.{FileUpload, InMemoryFileUpload, OnDiskFileUpload}
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.Controller
import com.twitter.inject.Logging
import com.twitter.io.Buf
import com.twitter.util.{Future, Return, Throw, Try}
import play.api.libs.json.{JsObject, JsValue, Json}
import sangria.ast
import sangria.execution.Executor.ExceptionHandler
import sangria.execution._
import sangria.marshalling.ResultMarshaller
import sangria.marshalling.playJson.{PlayJsonInputParser, _}
import sangria.parser.QueryParser
import sangria.schema.{Field, ObjectType, Schema, fields}
import sangria.validation.Violation
import com.sgw.example.utils.ExtendedScalaFuture._
import sangria.execution.deferred.DeferredResolver
import sangria.renderer.SchemaRenderer

import scala.concurrent.ExecutionContext
import scala.io.Source
import scala.util.{Failure, Success}

case class GraphQLController(
  pingService: PingService,
  fooService: FooService
)(
  implicit val ioExecutionContext: ExecutionContext
) extends Controller with Logging {

  private val querySchema = ObjectType("Queries",
    fields[ExampleGraphQLContext, Unit](
      Field("Ping", PingSchema.queries, resolve = _.ctx.pingService),
      Field("Foo", FooSchema.queries, resolve = _.ctx.fooService),
    )
  )

  private val mutationSchema = ObjectType("Mutations",
    fields[ExampleGraphQLContext, Unit](
      Field("Foo", FooSchema.mutations, resolve = _.ctx.fooService),
    )
  )

  private val schema = Schema(querySchema, Some(mutationSchema))

  info(SchemaRenderer.renderSchema(schema))

  private val exceptionHandler: ExceptionHandler = new ExceptionHandler(
    onException = {
      case (rm: ResultMarshaller, ex: RuntimeException) =>
        info(ex.getMessage, ex)
        HandledException(ex.getMessage)
      // handle all non-explicit messages
      case (rm: ResultMarshaller, ex: Throwable) =>
        error(ex.getMessage, ex)
        HandledException("An unexpected error occurred processing your request")
    },
    onViolation = {
      case (_: ResultMarshaller, v: Violation) =>
        info(v.errorMessage)
        HandledException(v.errorMessage)
    },
    onUserFacingError = {
      case (_: ResultMarshaller, err: UserFacingError) =>
        info(err.getMessage())
        HandledException(err.getMessage())
    }
  )

  get[Request, Future[Response]]("/graphql") { request: Request =>
//    val authInfo = request.ssoAuthInfo
    val query = request.params.get("query")
    val variables = request.params.get("variables")

    // this could be implemented as a filter if needed later
    handleQuery(
      query,
      variables,
      ExampleGraphQLContext(
        pingService,
        fooService,
        Map[String, Seq[FileUpload]]()
      ),
      exceptionHandler
    )
  }

  post[Request, Future[Response]]("/graphql") { request: Request =>
    Future.const {
      getQueryPartsFromRequest(request)
    } flatMap { case (query, variables, fileParts) =>
      handleQuery(
        query,
        variables,
        ExampleGraphQLContext(
          pingService,
          fooService,
          fileParts
        ),
        exceptionHandler
      )
    }
  }

  private def handleQuery(
    maybeQuery: Option[String],
    maybeVariables: Option[String],
    context: ExampleGraphQLContext,
    exHandler: ExceptionHandler
  ): Future[Response] = maybeQuery.filter { query =>
    query.trim.nonEmpty
  }.map { query =>
    executeQuery(query, maybeVariables, context, exHandler)
  }.getOrElse {
    Future.value(response.badRequest("query parameter must be defined"))
  }

  /**
    * @param query     the graphql query to execute
    * @param variables the variables to be available to the query
    * @param context   the execution context to run under
    * @return Future Response object based on the query result
    */
  private def executeQuery(
    query: String,
    variables: Option[String],
    context: ExampleGraphQLContext,
    exHandler: ExceptionHandler
  ): Future[Response] = Future {
    QueryParser.parse(query)
  }.flatMap {
    case Success(query) =>
      println(s"query:\n${query.renderPretty}")

      val vars: JsValue = variables match {
        case Some(s: String) if s.trim.nonEmpty && !s.trim.equals("undefined") => PlayJsonInputParser.parse(s).get
        case _ => JsObject(Nil)
      }

      println(s"vars:\n$vars")

      // internally we use scala Futures
      execute(query, vars, context, exHandler).map {
        // a recover on a future triggers the success with a  response object
        case resp: Response => resp
        // success case without a recovery should always be a JsValue
        case jsv: JsValue => response.ok(jsv.toString)
        // default case
        case value => response.internalServerError(s"Unexpected error occurred query:$query variables: $variables")
      } handle {
        case e: ValidationError =>
          Try {
            error(s"${e.getMessage}\n\nquery:\n${query.renderPretty}\n\nvariables:\n$vars", e)
          }
          response.badRequest(e.resolveError)
        case e: QueryReducingError =>
          Try {
            error(e.getMessage, e)
          }
          response.badRequest(e.resolveError)
        case e: QueryAnalysisError =>
          Try {
            error(e.getMessage, e)
          }
          response.badRequest(e.resolveError)
        case e: ErrorWithResolver =>
          Try {
            error(e.getMessage, e)
          }
          response.internalServerError(e.resolveError)
        case e: Throwable =>
          Try {
            error(e, e)
          }
          response.internalServerError("Unexpected error occurred.")
        case value =>
          Try {
            error(value.toString, value)
          }
          response.internalServerError("Unexpected error occurred.")
      }

    case Failure(error) => error match {
      case e: ValidationError => Future.value(
        response.badRequest(
          e.resolveError
        )
      )
      case _ => Future.value(response.badRequest(error.getMessage))
    }

  }.map(resp => {
    resp.setContentTypeJson
    resp
  })

  private def execute(
    query: ast.Document,
    variables: JsValue,
    context: ExampleGraphQLContext,
    exHandler: ExceptionHandler
  ): Future[JsValue] = Executor.execute(
    schema,
    query,
    context,
    variables = variables,
    exceptionHandler = exceptionHandler,
    deferredResolver = DeferredResolver.fetchers(
      GraphQLBar.gqlDeferredFetcher
    )
  ).toTwitterFuture

  private def getQueryPartsFromRequest(request: Request): Try[(Option[String], Option[String], Map[String, Seq[FileUpload]])] =
    request.contentType match {
      case Some(value) if value.startsWith("application/json") => getQueryPartsFromBody(request)
      case Some(value) if value.startsWith("multipart/form-data") => getQueryPartsFromMultipart(request)
      case Some(value) => Throw(new RuntimeException(s"Invalid content type '$value'."))
      case None => Throw(new RuntimeException("No content type specified."))
    }

  private def getQueryPartsFromBody(
    request: Request
  ): Try[(Option[String], Option[String], Map[String, Seq[FileUpload]])] = bodyGraphQLMap(request.getInputStream()).map { graphQLQuery =>
    (graphQLQuery.query, graphQLQuery.variables, Map[String, Seq[FileUpload]]())
  }

  private def bodyGraphQLMap(is: InputStream): Try[GraphQLQuery] = Try {
    Json.parse(is).as[GraphQLQuery]
  }

  private def getQueryPartsFromMultipart(request: Request): Try[(Option[String], Option[String], Map[String, Seq[FileUpload]])] =
    request.multipart match {
      case Some(parts: Multipart) =>
        Return((formPartAsString(parts, "query"), formPartAsString(parts, "variables"), parts.files))
      case None =>
        Throw(new RuntimeException("Invalid GraphQL POST."))
      case _ =>
        error(s"Unable to find query in request parts ${request.multipart}")
        Throw(new RuntimeException("Invalid GraphQL POST."))
    }

  private def formPartAsString(parts: Multipart, name: String): Option[String] = {
    parts.files.get(name) match {
      case Some(List(queryPart: OnDiskFileUpload)) => Some(Source.fromFile(queryPart.content).mkString)
      case Some(List(queryPart: InMemoryFileUpload)) =>
        queryPart.content match {
          case Buf.ByteArray.Owned(bytes, _, _) => Some(new String(bytes, Charset.forName("UTF-8")))
          case _ => None
        }
      case _ => None
    }
  } match {
    case None => parts.attributes.get(name) match {
      case Some(values: Seq[String]) => Some(values.head)
      case _ => None
    }
    case value => value
  }
}
