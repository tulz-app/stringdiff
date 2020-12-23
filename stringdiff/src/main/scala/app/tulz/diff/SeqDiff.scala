package app.tulz.diff

import app.tulz.diff.util.DiffCollapse

import scala.collection.IndexedSeqView

object SeqDiff {

  def seq[A](
    s1: IndexedSeq[A],
    s2: IndexedSeq[A],
    collapse: Boolean = true
  ): List[DiffElement[IndexedSeq[A]]] =
    apply(s1.view, s2.view, collapse).map(_.map(_.toIndexedSeq))

  def apply[A](
    s1: IndexedSeqView[A],
    s2: IndexedSeqView[A],
    collapse: Boolean = true
  ): List[DiffElement[IndexedSeqView[A]]] = {
    val myersDiff = MyersDiff.diff(s1, s2)
    val diff      = MyersInterpret(myersDiff, s1, s2)
    if (collapse) {
      DiffCollapse(diff)
    } else {
      diff
    }
  }

}
