//
//  JsonTest.scala
//  ahql
//
//  Created by d-exclaimation on 2:34 PM.
//

package io.github.dexclaimation.ahql

import io.github.dexclaimation.ahql.graphql.{GqlError, GqlResponse}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import spray.json.{JsArray, JsNull, JsObject, JsString}

class JsonTest extends AnyWordSpec with Matchers {
  "GqlError" should {

    "return a JsObject following GraphQL Spec" in {
      val error = GqlError("This should be an error")
      error.getFields("message") match {
        case Seq(JsString("This should be an error")) => succeed
        case _ => fail("Missing error field!!")
      }
    }

    "return a JsArray with the JsObject following the GraphQL Spec" in {
      val errorResponse = GqlError.of("#1", "#2", "#3", "#4")
      val elements = errorResponse.elements
      elements
        .indices
        .map(i => i -> s"#${i + 1}")
        .map {
          case (i, msg) => elements(i) match {
            case JsObject(fields) => fields("message") match {
              case JsString(`msg`) => succeed
              case _ => fail("Message should be #i where i is index + 1 and in the form of JsString")
            }
            case _ => fail("GqlError JsObject expected but not defined")
          }
        }
    }
  }

  "GqlResponse" should {

    "return a JsObject with `data` but no `errors` when successful" in {
      val res = GqlResponse.ok(JsObject.empty)
      val JsObject(fields) = res

      assertResult(JsObject.empty) {
        fields("data")
      }

      assertThrows[NoSuchElementException] {
        fields("errors")
      }
    }

    "return an `errors` array and `data` as JsNull for execution error" in {
      val res = GqlResponse.error("#0", "#1", "#2")
      val JsObject(fields) = res

      assertResult(JsNull) {
        fields("data")
      }

      fields("errors") match {
        case JsArray(elements) => elements.map {
          case JsObject(fields) => fields("message") match {
            case JsString(_) => succeed
            case _ => fail("Message should be #i where i is index + 1 and in the form of JsString")
          }
        }
        case _ => fail("Errors should be in an JsArray")
      }
    }
  }
}
