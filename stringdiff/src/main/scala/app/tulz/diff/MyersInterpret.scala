package app.tulz.diff

import app.tulz.diff.MyersDiff.Operation

import scala.collection.mutable.ListBuffer
import scala.collection.IndexedSeqView

object MyersInterpret {

  import DiffElement._
  import Operation._

  def apply[A](
    ops: Seq[Operation],
    s1: IndexedSeqView[A],
    s2: IndexedSeqView[A]
  ): List[DiffElement[IndexedSeqView[A]]] =
    if (ops.isEmpty) {
      List(DiffElement.InBoth(s1))
    } else {
      val buffer = new ListBuffer[DiffElement[IndexedSeqView[A]]]
      buffer.sizeHint(ops.length)
      (Start +: ops).zip(ops :+ End).foreach {
        case (Start, Delete(deleteFrom, deleteCount)) =>
          if (deleteFrom > 0) {
            buffer.addOne(
              InBoth(
                s1.take(deleteFrom)
              )
            )
          }
          buffer.addOne(
            InFirst(
              s1.slice(deleteFrom, deleteFrom + deleteCount)
            )
          )

        case (Start, Insert(insertAt, insertFrom, insertCount)) =>
          if (insertAt > 0) {
            buffer.addOne(
              InBoth(
                s1.take(insertFrom)
              )
            )
          }
          buffer.addOne(
            InSecond(
              s2.slice(insertFrom, insertFrom + insertCount)
            )
          )

        case (Delete(deleteFrom, deleteCount), Insert(insertAt, insertFrom, insertCount)) =>
          if (insertAt > deleteFrom + deleteCount) {
            buffer.addOne(
              InBoth(
                s1.slice(deleteFrom + deleteCount, insertAt)
              )
            )
          }
          buffer.addOne(
            InSecond(
              s2.slice(insertFrom, insertFrom + insertCount)
            )
          )

        case (Insert(insertAt, _, _), Delete(deleteFrom, deleteCount)) =>
          if (deleteFrom > insertAt) {
            buffer.addOne(
              InBoth(
                s1.slice(insertAt, deleteFrom)
              )
            )
          }
          buffer.addOne(
            InFirst(
              s1.slice(deleteFrom, deleteFrom + deleteCount)
            )
          )

        case (Insert(insertAt1, _, _), Insert(insertAt2, insertFrom2, insertCount2)) =>
          if (insertAt2 > insertAt1) {
            buffer.addOne(
              InBoth(
                s1.slice(insertAt1, insertAt2)
              )
            )
          }
          buffer.addOne(
            InSecond(
              s2.slice(insertFrom2, insertFrom2 + insertCount2)
            )
          )

        case (Delete(deleteFrom1, deleteCount1), Delete(deleteFrom2, deleteCount2)) =>
          if (deleteFrom2 > deleteFrom1 + deleteCount1) {
            buffer.addOne(
              InBoth(
                s1.slice(deleteFrom1 + deleteCount1, deleteFrom2)
              )
            )
          }
          buffer.addOne(
            InFirst(
              s1.slice(deleteFrom2, deleteFrom2 + deleteCount2)
            )
          )

        case (Insert(insertAt, _, _), End) =>
          if (s1.size > insertAt) {
            buffer.addOne(
              InBoth(
                s1.slice(insertAt, s1.size)
              )
            )
          }

        case (Delete(deleteFrom, deleteCount), End) =>
          if (s1.size > deleteFrom + deleteCount) {
            buffer.addOne(
              InBoth(
                s1.slice(deleteFrom + deleteCount, s1.size)
              )
            )
          }

        case (_, End)   =>
        case (_, Start) =>
        case (End, _)   =>

      }
      buffer.toList
    }

}
