//
//  ServerTest.scala
//  ahql
//
//  Created by d-exclaimation on 2:33 PM.
//

package io.github.dexclaimation.ahql

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes.{BadRequest, OK}
import akka.http.scaladsl.server.Directives.path
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import sangria.schema.{Field, ObjectType, Schema, StringType, fields}
import spray.json.{JsObject, JsString, JsValue}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class ServerTest extends AnyWordSpec with Matchers with ScalatestRouteTest with SprayJsonSupport {

  val schema = Schema(
    ObjectType("Query",
      fields[Unit, Unit](
        Field("helloWorld", StringType, resolve = _ => "Hello World!"),
      )
    )
  )

  val ahqlServer = new AhqlServer(schema, ())

  "AhqlServer" should {

    "return a route when applying middleware" in {
      ahqlServer.applyMiddleware(()) match {
        case _: Route => succeed
        case _ => fail("Must return route when applying middleware")
      }
    }

    "return a Future of StatusCodes and JsValue when serving" in {
      val fut = ahqlServer.serve(JsObject.empty, ())
      Await.result(fut, Duration.Inf) match {
        case BadRequest -> JsObject(_) => succeed
        case _ => fail("Invalid request should return BadRequest 404")
      }
    }

    val smallRoute = path("graphql") {
      ahqlServer.applyMiddleware(())
    }

    "return a BadRequest on invalid request to POST `/graphql`" in {
      Post("/graphql", JsObject.empty) ~> smallRoute ~> check {
        status shouldEqual BadRequest
      }
    }

    "return a 200 Ok with `Hello World!` on valid request to POST `/graphql`" in {
      val query = "query { helloWorld }"
      val req = JsObject("query" -> JsString(query))

      Post("/graphql", req) ~> smallRoute ~> check {
        status shouldEqual OK
        responseAs[JsValue] match {
          case JsObject(resp) => resp("data") match {
            case JsObject(data) => data("helloWorld") shouldEqual JsString("Hello World!")
            case _ => fail("Missing response `helloWorld`")
          }
          case _ => fail("Response is not in valid JsObject")
        }
      }
    }

    "return the same result on valid request to GET `/graphql`" in {
      val query = "query{helloWorld}"
      Get(s"/graphql?query=$query") ~> smallRoute ~> check {
        status shouldEqual OK
        responseAs[JsValue] match {
          case JsObject(resp) => resp("data") match {
            case JsObject(data) => data("helloWorld") shouldEqual JsString("Hello World!")
            case _ => fail("Missing response `helloWorld`")
          }
          case _ => fail("Response is not in valid JsObject")
        }
      }
    }
  }
}
