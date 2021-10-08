//
//  AhqlClient.scala
//  ahql
//
//  Created by d-exclaimation on 10:14 PM.
//

package io.github.dexclaimation.ahql

import akka.actor.ClassicActorSystemProvider
import akka.http.scaladsl.Http
import akka.http.scaladsl.client.RequestBuilding.{Get, Post}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ContentTypes, HttpHeader, HttpMethod, HttpMethods, HttpRequest, headers => h}
import akka.http.scaladsl.unmarshalling.Unmarshal
import io.github.dexclaimation.ahql.graphql.GqlRequest
import spray.json.{DefaultJsonProtocol, JsObject, JsValue, JsonParser}

import scala.concurrent.{ExecutionContext, Future}

/**
 * Minimal GraphQL HTTP-based Client
 *
 * @param endpoint       GraphQL API Endpoint.
 * @param defaultHeaders Default HTTP Headers.
 */
class AhqlClient(
  endpoint: String,
  defaultHeaders: Seq[HttpHeader] = Nil
)(implicit system: ClassicActorSystemProvider) extends SprayJsonSupport with DefaultJsonProtocol {

  /**
   * Make a fetch request to a GraphQL API.
   *
   * @param query         Query AST Document.
   * @param operationName Operation name being executed from the AST.
   * @param variables     Variables used in the query.
   * @param headers       Additional header for this request.
   * @return Future of an JsObject
   */
  def fetch(
    query: sangria.ast.Document,
    operationName: Option[String] = None,
    variables: JsObject = JsObject.empty,
    method: HttpMethod = HttpMethods.POST,
    headers: Seq[HttpHeader] = Nil
  )(implicit ex: ExecutionContext): Future[JsObject] = method match {
    case HttpMethods.POST =>
      val payload = operationName
        .map(GqlRequest(query, _, variables))
        .getOrElse(GqlRequest(query, variables))

      post(payload, headers)

    case HttpMethods.GET =>
      val queryParams = GqlRequest.queryString(query, operationName, variables)
      get(queryParams, headers)


    case _ => Future.failed(new Error("Cannot perform GraphQL Request other than in POST and GET"))
  }

  /**
   * Make a fetch request to a GraphQL API, but not
   * hard bound to [[spray.json.JsObject]].
   *
   * @param query         Query AST Document.
   * @param operationName Operation name being executed from the AST.
   * @param variables     Variables used in the query.
   * @param headers       Additional header for this request.
   * @return Future of an JsObject
   */
  def _fetch(
    query: sangria.ast.Document,
    operationName: Option[String] = None,
    variables: String = JsObject.empty.compactPrint,
    method: HttpMethod = HttpMethods.POST,
    headers: Seq[HttpHeader] = Nil
  )(implicit ex: ExecutionContext): Future[JsObject] = JsonParser(variables) match {
    case obj: JsObject => fetch(query, operationName, obj, method, headers)
    case _ => Future.failed(new Error("Cannot parse variables"))
  }


  /**
   * Make HTTP GET Request
   *
   * @param headers Additional headers.
   */
  private def get(
    queryParams: String,
    headers: Seq[HttpHeader]
  )(implicit ex: ExecutionContext): Future[JsObject] = {
    val req = Get(s"$endpoint?$queryParams")
      .withHeaders(
        headers ++ defaultHeaders
      )
    httpRequest(req)
  }

  /**
   * Make HTTP POST Request
   *
   * @param headers Additional headers.
   */
  private def post(
    js: JsValue,
    headers: Seq[HttpHeader]
  )(implicit ex: ExecutionContext): Future[JsObject] = {
    val req = Post(endpoint, js)
      .withHeaders(
        headers ++ defaultHeaders :+ h.`Content-Type`(ContentTypes.`application/json`)
      )
    httpRequest(req)
  }

  /**
   * Make HTTP Request
   *
   * @param req HttpRequest
   */
  private def httpRequest(req: HttpRequest)
    (implicit ex: ExecutionContext): Future[JsObject] =
    Http()
      .singleRequest(req)
      .map(Unmarshal(_))
      .flatMap(_.to[JsObject])

  private[ahql] def test(
    query: sangria.ast.Document,
    operationName: Option[String] = None,
    variables: JsObject = JsObject.empty,
    method: HttpMethod = HttpMethods.POST,
    headers: Seq[HttpHeader] = Nil
  )(implicit ex: ExecutionContext): HttpRequest = method match {
    case HttpMethods.POST =>
      val payload = operationName
        .map(GqlRequest(query, _, variables))
        .getOrElse(GqlRequest(query, variables))

      Post(endpoint, payload)
        .withHeaders(
          headers ++ defaultHeaders :+ h.`Content-Type`(ContentTypes.`application/json`)
        )

    case HttpMethods.GET =>
      val queryParams = GqlRequest.queryString(query, operationName, variables)

      Get(s"$endpoint?$queryParams")
        .withHeaders(
          headers ++ defaultHeaders
        )

    case _ => throw new Error("Cannot perform GraphQL Request other than in POST and GET")
  }
}
