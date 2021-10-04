//
//  AhqlServer.scala
//  ahql
//
//  Created by d-exclaimation on 4:11 AM.
//

package io.github.dexclaimation.ahql

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes.BadRequest
import akka.http.scaladsl.server.Directives.{_string2NR, as, complete, concat, entity, get, parameters, post}
import akka.http.scaladsl.server.Route
import io.github.dexclaimation.ahql.graphql.GqlResponse
import io.github.dexclaimation.ahql.implicits._
import sangria.ast
import sangria.execution._
import sangria.execution.deferred.DeferredResolver
import sangria.parser.QueryParser
import sangria.schema.Schema
import sangria.validation.QueryValidator
import spray.json.{DefaultJsonProtocol, JsObject, JsValue, enrichAny}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag

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
class AhqlServer[Ctx, Val: ClassTag](
  schema: Schema[Ctx, Val],
  root: Val,
  queryValidator: QueryValidator = QueryValidator.default,
  deferredResolver: DeferredResolver[Ctx] = DeferredResolver.empty,
  exceptionHandler: ExceptionHandler = ExceptionHandler.empty,
  deprecationTracker: DeprecationTracker = DeprecationTracker.empty,
  middleware: List[Middleware[Ctx]] = Nil,
  maxQueryDepth: Option[Int] = None,
  queryReducers: List[QueryReducer[Ctx, _]] = Nil
) extends SprayJsonSupport with DefaultJsonProtocol {
  /**
   * Route Handler for GraphQL
   *
   * @param ctx Context for the schema.
   * @return A route that already take an entity and complete with the proper results.
   */
  def applyMiddleware(ctx: Ctx)(implicit ex: ExecutionContext): Route = concat(
    (post & entity(as[JsValue])) { js =>
      complete(serve(js, ctx))
    },
    (get & parameters("query", "variables".optional, "operationName".optional)) { (query, variables, ops) =>
      complete(serverOverGet(query, variables, ops, ctx))
    }
  )

  /**
   * Execution Handler for GraphQL '''POST'''
   *
   * @param ctx Context for the schema.
   * @return A Future of Status Code with a response JsValue.
   */
  def serve(js: JsValue, ctx: Ctx)(implicit ex: ExecutionContext): Future[(StatusCode, JsValue)] = js
    .gqlContext
    .map { case (queryAst, vars, operation) => execute(ctx, queryAst, vars, operation) }
    .unwrapOr { e => Future.successful(BadRequest -> GqlResponse.error(e.getMessage)) }


  private def serverOverGet(
    query: String, variables: Option[String], operation: Option[String], ctx: Ctx
  )(implicit ex: ExecutionContext): Future[(StatusCode, JsValue)] = QueryParser
    .parse(query)
    .map(execute(ctx, _, variables.map(_.toJson).getOrElse(JsObject.empty).asJsObject, operation))
    .unwrapOr(e => Future.successful(BadRequest -> GqlResponse.error(e.getMessage)))


  private def execute(ctx: Ctx, queryAst: ast.Document, vars: JsObject, operation: Option[String])
    (implicit ex: ExecutionContext): Future[(StatusCode, JsValue)] = Executor
    .handle(
      queryAst = queryAst,
      vars = vars,
      operation = operation,
      schema = schema,
      ctx = ctx,
      root = root,
      queryValidator = queryValidator,
      deferredResolver = deferredResolver,
      exceptionHandler = exceptionHandler,
      deprecationTracker = deprecationTracker,
      middleware = middleware,
      maxQueryDepth = maxQueryDepth,
      queryReducers = queryReducers,
    )
}
