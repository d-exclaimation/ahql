//
//  HttpMethodStrategy.scala
//  ahql
//
//  Created by d-exclaimation on 3:58 PM.
//

package io.github.dexclaimation.ahql.utils

/**
 * Http Method routing strategy
 */
sealed trait HttpMethodStrategy

object HttpMethodStrategy {
  private[ahql] case object EnableAll extends HttpMethodStrategy

  private[ahql] case object EnableOnlyPost extends HttpMethodStrategy

  private[ahql] case object EnableOnlyGet extends HttpMethodStrategy

  private[ahql] case object EnableRESTLike extends HttpMethodStrategy

  private[ahql] case object EnableGetForQuery extends HttpMethodStrategy

  private[ahql] case object EnableNone extends HttpMethodStrategy

  /** Default Strategy, Only allow request on `POST` */
  val onlyPost: HttpMethodStrategy = EnableOnlyPost

  /** All both `POST` & `GET` for all operations */
  val enableAll: HttpMethodStrategy = EnableAll

  /** Only allow request on `GET` */
  val onlyGet: HttpMethodStrategy = EnableOnlyGet

  /** Allow all operations on `POST` but only Query for `GET` */
  val queryOnlyGet: HttpMethodStrategy = EnableGetForQuery

  /** Allow only Query for `GET` and only Mutation for `POST` */
  val restLikeStandard: HttpMethodStrategy = EnableRESTLike

  /** Disable the routing entirely */
  val none: HttpMethodStrategy = EnableNone
}
