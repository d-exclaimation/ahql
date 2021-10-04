//
//  TryKeep.scala
//  ahql
//
//  Created by d-exclaimation on 3:26 AM.
//

package io.github.dexclaimation.ahql.utils

import akka.stream.scaladsl.Keep

import scala.util.Try

object TKeep {
  def both[R, L](other: => Try[L]): R => Try[(R, L)] =
    r => other.map(Keep.both(r, _))

  def threeR[R1, R2, L](other: => Try[L]): ((R1, R2)) => Try[(R1, R2, L)] =
    r => other.map(FlatKeep.bothR(r, _))
}
