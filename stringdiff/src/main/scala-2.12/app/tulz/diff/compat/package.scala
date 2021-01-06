package app.tulz.diff

import scala.collection.IndexedSeqView
import scala.util.matching.Regex

package object compat {

  implicit class RegExOps(val underlying: Regex) extends AnyVal {

    def matches(s: String): Boolean = underlying.findFirstIn(s).isDefined

  }

  implicit class IndexedSeqOps[A](val underlying: IndexedSeq[A]) extends AnyVal {

    def concat(other: IndexedSeqView[A]): IndexedSeqView[A] = underlying ++ other

    def indexedView: IndexedSeqView[A] = underlying

  }

  implicit class StringOps(val underlying: String) extends AnyVal {

    def indexedView: IndexedSeqView[Char] = underlying.toIndexedSeq

  }

}
