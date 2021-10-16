//
//  Ahql.scala
//  ahql
//
//  Created by d-exclaimation on 3:25 AM.
//
package io.github.dexclaimation.ahql

import akka.actor.ClassicActorSystemProvider
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpHeader, HttpMethod, HttpMethods, StatusCode}
import akka.http.scaladsl.server.Route
import io.github.dexclaimation.ahql.utils.HttpMethodStrategy
import sangria.execution._
import sangria.execution.deferred.DeferredResolver
import sangria.schema.Schema
import sangria.validation.QueryValidator
import spray.json.{JsObject, JsValue}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

object Ahql extends SprayJsonSupport {

  type Server[Ctx, Val] = AhqlServer[Ctx, Val]

  /**
   * Minimal GraphQL Server Middleware and Router
   *
   * @param schema             Sangria GraphQL Schema.
   * @param root               Root value object for the schema.
   * @param queryValidator     Executor queryValidator.
   * @param deferredResolver   Any deferred resolver used by the executor.
   * @param exceptionHandler   Query Exception Handlers.
   * @param deprecationTracker Deprecation Trackers used by the executor.
   * @param middleware         Resolver middleware.
   * @param maxQueryDepth      Limit of the query depth can be resolved.
   * @param queryReducers      Query reducers for resolvers.
   */
  def createServer[Ctx, Val: ClassTag](
    schema: Schema[Ctx, Val],
    root: Val,
    httpMethodStrategy: HttpMethodStrategy = HttpMethodStrategy.onlyPost,
    queryValidator: QueryValidator = QueryValidator.default,
    deferredResolver: DeferredResolver[Ctx] = DeferredResolver.empty,
    exceptionHandler: ExceptionHandler = ExceptionHandler.empty,
    deprecationTracker: DeprecationTracker = DeprecationTracker.empty,
    middleware: List[Middleware[Ctx]] = Nil,
    maxQueryDepth: Option[Int] = None,
    queryReducers: List[QueryReducer[Ctx, _]] = Nil
  ): Server[Ctx, Val] = new AhqlServer[Ctx, Val](
    schema, root, httpMethodStrategy, queryValidator, deferredResolver, exceptionHandler,
    deprecationTracker, middleware, maxQueryDepth, queryReducers
  )

  type Client = AhqlClient

  /**
   * Minimal GraphQl HTTP-Based Client
   *
   * @param endpoint       Endpoint URL for the GraphQL API.
   * @param defaultHeaders default headers on all fetch / request.
   * @param provider       ActorSystem provider to execute request
   */
  def createClient(
    endpoint: String,
    defaultHeaders: Seq[HttpHeader] = Nil
  )(implicit provider: ClassicActorSystemProvider): Client = new AhqlClient(
    endpoint, defaultHeaders
  )

  /**
   * GraphQL Route Handler for `akka-http` and `spray-json`.
   *
   * @param schema             Sangria GraphQL Schema.
   * @param ctx                Context for the schema.
   * @param root               Root value object for the schema.
   * @param queryValidator     Executor queryValidator.
   * @param deferredResolver   Any deferred resolver used by the executor.
   * @param exceptionHandler   Query Exception Handlers.
   * @param deprecationTracker Deprecation Trackers used by the executor.
   * @param middleware         Resolver middleware.
   * @param maxQueryDepth      Limit of the query depth can be resolved.
   * @param queryReducers      Query reducers for resolvers.
   * @return A route that already take an entity and complete with the proper results.
   */
  def applyMiddleware[Ctx, Val: ClassTag](
    schema: Schema[Ctx, Val],
    ctx: Ctx,
    root: Val,
    httpMethodStrategy: HttpMethodStrategy = HttpMethodStrategy.onlyPost,
    queryValidator: QueryValidator = QueryValidator.default,
    deferredResolver: DeferredResolver[Ctx] = DeferredResolver.empty,
    exceptionHandler: ExceptionHandler = ExceptionHandler.empty,
    deprecationTracker: DeprecationTracker = DeprecationTracker.empty,
    middleware: List[Middleware[Ctx]] = Nil,
    maxQueryDepth: Option[Int] = None,
    queryReducers: List[QueryReducer[Ctx, _]] = Nil
  )(implicit ex: ExecutionContext): Route = {
    val server = new AhqlServer[Ctx, Val](
      schema, root, httpMethodStrategy, queryValidator, deferredResolver, exceptionHandler,
      deprecationTracker, middleware, maxQueryDepth, queryReducers
    )
    server.applyMiddleware(ctx)
  }


  /**
   * GraphQL Execution Handler for `akka-http` and `spray-json`.
   *
   * @param schema             Sangria GraphQL Schema.
   * @param ctx                Context for the schema.
   * @param root               Root value object for the schema.
   * @param queryValidator     Executor queryValidator.
   * @param deferredResolver   Any deferred resolver used by the executor.
   * @param exceptionHandler   Query Exception Handlers.
   * @param deprecationTracker Deprecation Trackers used by the executor.
   * @param middleware         Resolver middleware.
   * @param maxQueryDepth      Limit of the query depth can be resolved.
   * @param queryReducers      Query reducers for resolvers.
   * @return A Future of Status Code with a response JsValue.
   */
  def serve[Ctx, Val: ClassTag](
    js: JsValue,
    schema: Schema[Ctx, Val],
    ctx: Ctx,
    root: Val,
    httpMethodStrategy: HttpMethodStrategy = HttpMethodStrategy.onlyPost,
    queryValidator: QueryValidator = QueryValidator.default,
    deferredResolver: DeferredResolver[Ctx] = DeferredResolver.empty,
    exceptionHandler: ExceptionHandler = ExceptionHandler.empty,
    deprecationTracker: DeprecationTracker = DeprecationTracker.empty,
    middleware: List[Middleware[Ctx]] = Nil,
    maxQueryDepth: Option[Int] = None,
    queryReducers: List[QueryReducer[Ctx, _]] = Nil
  )(implicit ex: ExecutionContext): Future[(StatusCode, JsValue)] = {
    val server = createServer[Ctx, Val](
      schema, root, httpMethodStrategy,
      queryValidator, deferredResolver, exceptionHandler,
      deprecationTracker, middleware, maxQueryDepth, queryReducers
    )
    server.serve(js, ctx)
  }

  /**
   * Make a fetch request to a GraphQL API.
   *
   * @param endpoint      URL Endpoint
   * @param query         Query AST Document.
   * @param operationName Operation name being executed from the AST.
   * @param variables     Variables used in the query.
   * @param headers       Additional header for this request.
   * @return Future of an JsObject
   */
  def fetch(
    endpoint: String,
    query: sangria.ast.Document,
    operationName: Option[String] = None,
    variables: JsObject = JsObject.empty,
    method: HttpMethod = HttpMethods.POST,
    headers: Seq[HttpHeader] = Nil
  )(implicit provider: ClassicActorSystemProvider, ex: ExecutionContext): Future[JsObject] = {
    val client = new AhqlClient(endpoint)
    client.fetch(query, operationName, variables, method, headers)
  }
}
