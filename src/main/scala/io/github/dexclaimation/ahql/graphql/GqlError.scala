//
//  GqlError.scala
//  ahql
//
//  Created by d-exclaimation on 3:52 AM.
//

package io.github.dexclaimation.ahql.graphql

import spray.json.{JsArray, JsObject, JsString}

object GqlError {
  /**
   * GraphQL Error with only Message
   *
   * @param message Error Message as String
   */
  def apply(message: String): JsObject = JsObject(
    "message" -> JsString(message)
  )


  /**
   * Custom Pattern matching for Error types
   */
  def unapply(arr: JsArray): Option[Vector[JsObject]] = Some(
    arr.elements.collect {
      case obj: JsObject => obj
    }
  )

  /**
   * Multiple GraphQL Error with only messages
   *
   * @param messages Error messages
   */
  def of(messages: String*): JsArray = JsArray(
    messages.map(apply): _*
  )
}