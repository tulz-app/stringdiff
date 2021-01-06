package app.tulz.diff

import app.tulz.diff.compat._
import app.tulz.diff.util.DiffCollapse

object SeqDiff {

  def apply[A](
    s1: IndexedSeq[A],
    s2: IndexedSeq[A],
    collapse: Boolean = true
  ): List[DiffElement[IndexedSeq[A]]] = {
    val myersDiff = MyersDiff.diff(s1.indexedView, s2.indexedView)
    val diff      = MyersInterpret(myersDiff, s1, s2)
    if (collapse) {
      DiffCollapse(diff)
    } else {
      diff
    }
  }

}
