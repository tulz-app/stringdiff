package app.tulz.diff

import scala.collection.SeqView
import scala.language.implicitConversions

package object compat {

  implicit def indexedSeq_ViewToIndexedSeqView[A](
    underlying: SeqView[A, IndexedSeq[A]]
  ): IndexedSeqView[A] = new IndexedSeqView[A](underlying)

  implicit def string_ViewToIndexedSeqView(
    underlying: SeqView[Char, String]
  ): IndexedSeqView[Char] = new IndexedSeqView[Char](underlying)

  implicit def indexedSeqViewOfCharOps(
    underlying: IndexedSeqView[Char]
  ): IndexedSeqViewOfCharOps = new IndexedSeqViewOfCharOps(underlying)

  implicit def indexedSeqViewOfStringOps(
    underlying: IndexedSeqView[String]
  ): IndexedSeqViewOfStringOps = new IndexedSeqViewOfStringOps(underlying)

}
