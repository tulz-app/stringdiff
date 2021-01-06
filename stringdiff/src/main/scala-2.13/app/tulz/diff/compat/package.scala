package app.tulz.diff

package object compat {

  implicit class IndexedSeqOps[A](val underlying: IndexedSeq[A]) extends AnyVal {

    def indexedView: scala.collection.IndexedSeqView[A] = underlying.view

  }

  implicit class StringOps(val underlying: String) extends AnyVal {

    def indexedView = underlying.view

  }



}
