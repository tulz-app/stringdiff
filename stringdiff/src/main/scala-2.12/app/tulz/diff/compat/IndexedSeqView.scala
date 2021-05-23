package app.tulz.diff
package compat

import scala.collection.SeqView
import scala.collection.immutable.Range

class IndexedSeqView[A](
  private[compat] val underlying: SeqView[A, _]
) {
  def apply(index: Int): A = underlying(index)

  def size: Int = underlying.length

  def isEmpty: Boolean = underlying.isEmpty

  def concat(other: IndexedSeqView[A]): IndexedSeqView[A] =
    new IndexedSeqView((underlying ++ other.underlying).view)

  def take(n: Int): IndexedSeqView[A] = new IndexedSeqView(underlying.take(n))

  def slice(from: Int, until: Int): IndexedSeqView[A] = new IndexedSeqView(underlying.slice(from, until))

  def toIndexedSeq: IndexedSeq[A] = underlying.toIndexedSeq

  def indices: Range = underlying.indices

  def forall(predicate: A => Boolean): Boolean = underlying.forall(predicate)
}
