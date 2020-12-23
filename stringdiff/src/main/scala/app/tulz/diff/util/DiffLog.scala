package app.tulz.diff.util

import app.tulz.diff.DiffElement
import app.tulz.diff.format.DiffFormat

import scala.collection.IndexedSeqView

private[diff] object DiffLog {

  def log(description: => String, diff: => List[DiffElement[IndexedSeqView[String]]]): Unit = {
    println(s"${description}:${" " * (20 - description.length)}${DiffFormat.ansi(diff.map(_.map(_.mkString)))}")
  }

  def logS(description: => String, diff: => List[DiffElement[String]]): Unit = {
    println(s"${description}:${" " * (20 - description.length)}${DiffFormat.ansi(diff)}")
  }

}
