//
//  implicits.scala
//  ahql
//
//  Created by d-exclaimation on 3:25 AM.
//

package io.github.dexclaimation.ahql

import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes.{BadRequest, InternalServerError, OK}
import io.github.dexclaimation.ahql.utils.TKeep
import sangria.ast
import sangria.execution._
import sangria.execution.deferred.DeferredResolver
import sangria.marshalling.sprayJson._
import sangria.parser.QueryParser
import sangria.schema.Schema
import sangria.validation.QueryValidator
import spray.json.{JsObject, JsString, JsValue, JsonFormat}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import scala.util.Try

package object implicits {

  implicit final class TryOrElse[T](t: Try[T]) {
    /** Get the value or return the value from a fallback function */
    def unwrapOr(fallback: Throwable => T): T = t.fold(fallback, identity)
  }

  implicit final class JsGraphQL(js: JsValue) {
    /** Get Variables from the request as Object */
    def variables: Try[JsObject] = Try {
      val JsObject(fields) = js
      fields
        .get("variables")
        .collect {
          case obj: JsObject => obj
        }
        .getOrElse(JsObject.empty)
    }

    /** Get Operation Name from request */
    def operationName: Try[Option[String]] = Try {
      val JsObject(fields) = js
      fields
        .get("operationName")
        .collect {
          case JsString(value) => value
        }
    }

    /** Get Query from request */
    def query: Try[String] = Try {
      val JsObject(fields) = js
      val JsString(query) = fields("query")
      query
    }


    /** Get and parse Query from request */
    def queryAst: Try[ast.Document] = query
      .flatMap(QueryParser.parse(_))


    /** Get all the relevant GraphQL Information */
    def gqlContext: Try[(ast.Document, JsObject, Option[String])] = queryAst
      .flatMap(TKeep.both(variables))
      .flatMap(TKeep.threeR(operationName))
  }

  implicit final class FutureGraphQL(e: Executor.type) {

    def handle[Ctx, Val: ClassTag](
      queryAst: ast.Document,
      vars: JsObject,
      operation: Option[String],
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
    )(implicit ex: ExecutionContext): Future[(StatusCode, JsValue)] = e
      .execute(
        schema = schema,
        queryAst = queryAst,
        userContext = ctx,
        root = root,
        operationName = operation,
        variables = vars,
        queryValidator = queryValidator,
        deferredResolver = deferredResolver,
        exceptionHandler = exceptionHandler,
        deprecationTracker = deprecationTracker,
        middleware = middleware,
        maxQueryDepth = maxQueryDepth,
        queryReducers = queryReducers,
      )
      .map(OK -> _)
      .recover {
        case error: QueryAnalysisError => BadRequest -> error.resolveError
        case error: ErrorWithResolver => InternalServerError -> error.resolveError
      }
  }

  implicit final class JsTraversal(js: JsValue) {
    def apply(access: String): JsValue = js match {
      case JsObject(fields) => fields(access)
      case _ => throw new NoSuchElementException(access)
    }

    def ?[T: JsonFormat](access: String): T = js(access).convertTo[T]
  }
}
