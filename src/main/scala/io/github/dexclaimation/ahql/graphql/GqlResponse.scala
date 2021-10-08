//
//  GqlResponse.scala
//  ahql
//
//  Created by d-exclaimation on 3:55 AM.
//

package io.github.dexclaimation.ahql.graphql

import spray.json.{JsArray, JsNull, JsObject, JsValue}

object GqlResponse {

  /**
   * Custom pattern matching for JsObject after GraphQL Request.
   */
  def unapply(resp: JsObject): Option[(Option[JsObject], Option[Vector[JsObject]])] = {
    val dataOpt = resp.fields.get("data")
    val errorOpt = resp.fields.get("errors")
    val result = (dataOpt, errorOpt) match {
      case (Some(data: JsObject), None) =>
        (Some(data), None)
      case (Some(data: JsObject), Some(GqlError(errors))) =>
        (Some(data), Some(errors))
      case (None, Some(GqlError(errors))) =>
        (None, Some(errors))
      case _ =>
        (None, Some(Vector(GqlError("Invalid response format"))))
    }
    Some(result)
  }

  /**
   * GraphQL Response that's successful
   *
   * @param data Successful data.
   */
  def ok(data: JsValue): JsObject = JsObject(
    "data" -> data
  )

  /**
   * GraphQL Response that's a failure
   *
   * @param errors Error messages compliant to GraphQL Spec.
   */
  def error(errors: JsArray): JsObject = JsObject(
    "data" -> JsNull,
    "errors" -> errors
  )


  /**
   * GraphQL Response that's a failure
   *
   * @param messages Error messages compliant to GraphQL Spec.
   */
  def error(messages: String*): JsObject = JsObject(
    "data" -> JsNull,
    "errors" -> GqlError.of(messages: _*)
  )

  /**
   * GraphQL Response that fails before execution.
   *
   * @param messages Error message compliant to GraphQl Spec
   */
  def reject(messages: String*): JsObject = JsObject(
    "errors" -> GqlError.of(messages: _*)
  )
}
