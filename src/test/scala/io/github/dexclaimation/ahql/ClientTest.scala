//
//  ClientTest.scala
//  ahql
//
//  Created by d-exclaimation on 12:15 AM.
//

package io.github.dexclaimation.ahql

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.server.Directives.path
import akka.http.scaladsl.testkit.ScalatestRouteTest
import io.github.dexclaimation.ahql.graphql.GqlResponse
import io.github.dexclaimation.ahql.implicits._
import io.github.dexclaimation.ahql.utils.HttpMethodStrategy
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import sangria.macros.LiteralGraphQLStringContext
import sangria.schema.{Field, ObjectType, Schema, StringType, fields}
import spray.json.{JsObject, JsString, JsValue}

class ClientTest extends AnyWordSpec with Matchers with SprayJsonSupport with ScalatestRouteTest {

  val schema = Schema(
    ObjectType("Query",
      fields[Unit, Unit](
        Field("helloWorld", StringType, resolve = _ => "Hello World!"),
      )
    )
  )

  val ahqlServer = new AhqlServer(schema, (),
    httpMethodStrategy = HttpMethodStrategy.enableAll
  )

  val route = path("test") {
    ahqlServer.applyMiddleware(())
  }

  val client = new AhqlClient("/test")

  "AhqlClient" when {

    "making GET request" should {

      "return proper result for query-only" in {
        val query = graphql"query { helloWorld }"
        client.test(query, method = HttpMethods.GET) ~> route ~> check {
          status shouldEqual OK
          responseAs[JsValue] match {
            case GqlResponse(Some(data), _) =>
              data("helloWorld") shouldEqual JsString("Hello World!")
            case _ => fail("Response is not in valid JsObject")
          }
        }
      }

      "return proper result for all required" in {
        val query = graphql"query MyOperation { helloWorld }"
        val operationName = Some("MyOperation")
        val variables = JsObject.empty
        client.test(query, operationName, variables, method = HttpMethods.GET) ~> route ~> check {
          status shouldEqual OK
          responseAs[JsValue] match {
            case GqlResponse(Some(data), _) =>
              data("helloWorld") shouldEqual JsString("Hello World!")
            case _ => fail("Response is not in valid JsObject")
          }
        }
      }
    }


    "making POST request" should {

      "return proper result for query-only" in {
        val query = graphql"query { helloWorld }"
        client.test(query) ~> route ~> check {
          status shouldEqual OK
          responseAs[JsValue] match {
            case GqlResponse(Some(data), _) =>
              data("helloWorld") shouldEqual JsString("Hello World!")
            case _ => fail("Response is not in valid JsObject")
          }
        }
      }

      "return proper result for all required" in {
        val query = graphql"query MyOperation { helloWorld }"
        val operationName = Some("MyOperation")
        val variables = JsObject.empty
        client.test(query, operationName, variables) ~> route ~> check {
          status shouldEqual OK
          responseAs[JsValue] match {
            case GqlResponse(Some(data), _) =>
              data("helloWorld") shouldEqual JsString("Hello World!")
            case _ => fail("Response is not in valid JsObject")
          }
        }
      }
    }
  }
}
