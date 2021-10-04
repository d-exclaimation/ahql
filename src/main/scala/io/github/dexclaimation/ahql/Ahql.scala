//
//  Ahql.scala
//  ahql
//
//  Created by d-exclaimation on 3:25 AM.
//
package io.github.dexclaimation.ahql

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.server.Route
import sangria.execution._
import sangria.execution.deferred.DeferredResolver
import sangria.schema.Schema
import sangria.validation.QueryValidator
import spray.json.JsValue

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

object Ahql extends SprayJsonSupport {

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
    queryValidator: QueryValidator = QueryValidator.default,
    deferredResolver: DeferredResolver[Ctx] = DeferredResolver.empty,
    exceptionHandler: ExceptionHandler = ExceptionHandler.empty,
    deprecationTracker: DeprecationTracker = DeprecationTracker.empty,
    middleware: List[Middleware[Ctx]] = Nil,
    maxQueryDepth: Option[Int] = None,
    queryReducers: List[QueryReducer[Ctx, _]] = Nil
  )(implicit ex: ExecutionContext): Route = {
    val server = new AhqlServer[Ctx, Val](
      schema, root, queryValidator, deferredResolver, exceptionHandler,
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
    queryValidator: QueryValidator = QueryValidator.default,
    deferredResolver: DeferredResolver[Ctx] = DeferredResolver.empty,
    exceptionHandler: ExceptionHandler = ExceptionHandler.empty,
    deprecationTracker: DeprecationTracker = DeprecationTracker.empty,
    middleware: List[Middleware[Ctx]] = Nil,
    maxQueryDepth: Option[Int] = None,
    queryReducers: List[QueryReducer[Ctx, _]] = Nil
  )(implicit ex: ExecutionContext): Future[(StatusCode, JsValue)] = {
    val server = new AhqlServer[Ctx, Val](
      schema, root, queryValidator, deferredResolver, exceptionHandler,
      deprecationTracker, middleware, maxQueryDepth, queryReducers
    )
    server.serve(js, ctx)
  }
}
