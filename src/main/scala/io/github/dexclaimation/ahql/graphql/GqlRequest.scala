//
//  GqlRequest.scala
//  ahql
//
//  Created by d-exclaimation on 10:19 PM.
//

package io.github.dexclaimation.ahql.graphql

import spray.json.{JsObject, JsString}

import java.net.URLEncoder

object GqlRequest {

  /**
   * GraphQL Request containing query, variables, and operationName
   *
   * @param query Query as [[sangria.ast.Document]]
   */
  def apply(
    query: sangria.ast.Document,
    operationName: String,
    variables: JsObject
  ): JsObject = JsObject(
    "query" -> JsString(query.renderCompact),
    "operationName" -> JsString(operationName),
    "variables" -> variables
  )

  /**
   * GraphQL Request containing query, and variables
   *
   * @param query Query as [[sangria.ast.Document]]
   */
  def apply(
    query: sangria.ast.Document,
    variables: JsObject
  ): JsObject = JsObject(
    "query" -> JsString(query.renderCompact),
    "variables" -> variables
  )


  /**
   * GraphQL Request containing query, and operationName
   *
   * @param query Query as [[sangria.ast.Document]]
   */
  def apply(
    query: sangria.ast.Document,
    operationName: String,
  ): JsObject = JsObject(
    "query" -> JsString(query.renderCompact),
    "operationName" -> JsString(operationName),
  )

  /**
   * GraphQL Request containing query
   *
   * @param query Query as [[sangria.ast.Document]]
   */
  def apply(
    query: sangria.ast.Document,
  ): JsObject = JsObject(
    "query" -> JsString(query.renderCompact),
  )

  private[ahql] def queryString(
    query: sangria.ast.Document, operationName: Option[String], variables: JsObject
  ): String = {
    val queryString = Vector("query" -> URLEncoder.encode(query.renderCompact, "UTF-8"))
    val others = Vector(
      operationName.map("operationName" -> _),
      (if (variables.fields.isEmpty) None else Some(variables.compactPrint)).map("variables" -> _)
    ).flatten

    (queryString ++ others)
      .map {
        case (key, value) => s"$key=$value"
      }
      .mkString("&")
  }
}
