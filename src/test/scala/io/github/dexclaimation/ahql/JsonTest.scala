//
//  JsonTest.scala
//  ahql
//
//  Created by d-exclaimation on 2:34 PM.
//

package io.github.dexclaimation.ahql

import io.github.dexclaimation.ahql.graphql.{GqlError, GqlRequest, GqlResponse}
import io.github.dexclaimation.ahql.implicits._
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import sangria.macros.LiteralGraphQLStringContext
import spray.json.DefaultJsonProtocol._
import spray.json.{DefaultJsonProtocol, JsArray, JsNull, JsObject, JsString, RootJsonFormat, enrichAny}

class JsonTest extends AnyWordSpec with Matchers {

  "GqlError" when {

    "applied" should {
      "return a JsObject following GraphQL Spec" in {
        val error = GqlError("This should be an error")
        error.getFields("message") match {
          case Seq(JsString("This should be an error")) => succeed
          case _ => fail("Missing error field!!")
        }
      }
    }

    "`.of` is called" should {
      "return a JsArray with the JsObject following the GraphQL Spec" in {
        val errorResponse = GqlError.of("#1", "#2", "#3", "#4")
        errorResponse match {
          case GqlError(vec) => vec
            .indices
            .map(i => i -> s"#${i + 1}")
            .map {
              case (i, msg) => vec(i)("message") match {
                case JsString(`msg`) => succeed
                case _ => fail("Message should be #i where i is index + 1 and in the form of JsString")
              }
            }
        }
      }
    }

    "GqlResponse" when {

      "successful" should {
        "return a JsObject with `data` but no `errors`" in {
          val res = GqlResponse.ok(JsObject.empty)

          assertResult(JsObject.empty) {
            res("data")
          }

          assertThrows[NoSuchElementException] {
            res("errors")
          }
        }
      }

      "execution error" should {
        "return an `errors` array and `data` as JsNull for execution error" in {
          val res = GqlResponse.error("#0", "#1", "#2")
          assertResult(JsNull) {
            res("data")
          }

          res("errors") match {
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

      "pattern matched" should {
        "return a tuple of `data` and `error`" in {
          GqlResponse.ok(JsObject("field" -> SampleData("hello").toJson)) match {
            case GqlResponse(Some(data), _) =>
              val res = data ?[SampleData] "field"
              assertResult("hello")(res.msg)

            case GqlResponse(None, _) => fail("Wrong result")
          }
        }
      }
    }
  }

  "GqlRequest" when {
    "applied" should {
      val query = graphql"query SomeOperation { queryField }"
      val operationName = "SomeOperation"
      val variables = JsObject.empty

      "return the appropriate JsObjects" in {
        val req1 = GqlRequest(query)
        val req2 = GqlRequest(query, variables)
        val req3 = GqlRequest(query, operationName, variables)

        assertResult(query.renderCompact) {
          req1 ?[String] "query"
        }

        assertResult(variables) {
          req2 ?[JsObject] "variables"
        }

        assertResult(operationName) {
          req3 ?[String] "operationName"
        }
      }
    }
  }

  case class SampleData(msg: String)

  object SampleData extends DefaultJsonProtocol {
    implicit val format: RootJsonFormat[SampleData] = jsonFormat1(SampleData.apply)
  }
}
