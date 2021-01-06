package app.tulz.diff

import app.tulz.diff.format.DiffFormat
import app.tulz.diff.util.DiffCollapse
import scala.collection.compat._
import app.tulz.diff.compat._

object StringDiff {

  def apply(
    s1: String,
    s2: String,
    collapse: Boolean = true
  ): String = ansi(s1, s2)

  def ansi(
    s1: String,
    s2: String,
    collapse: Boolean = true
  ): String =
    DiffFormat.ansi(diff(s1, s2, collapse))

  def ansiBoth(
    s1: String,
    s2: String,
    collapse: Boolean = true
  ): (String, String) =
    DiffFormat.ansiBoth(diff(s1, s2, collapse))

  def text(
    s1: String,
    s2: String,
    collapse: Boolean = true
  ): String =
    DiffFormat.text(diff(s1, s2, collapse))

  def diff(
    s1: String,
    s2: String,
    collapse: Boolean = true
  ): List[DiffElement[String]] = {
    val myersDiff = MyersDiff.diff(s1.indexedView, s2.indexedView)
    val diff      = MyersInterpret(myersDiff, s1.indexedView, s2.indexedView)
    val result = if (collapse) {
      DiffCollapse(diff)
    } else {
      diff
    }
    result.map(_.map(_.mkString))
  }

}
