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
}
