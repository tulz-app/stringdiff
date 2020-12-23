package app.tulz.diff.format

import app.tulz.diff.DiffElement

trait DiffFormat[Out] {

  def apply(diff: List[DiffElement[String]]): Out

}

object DiffFormat {

  val ansi: DiffFormat[String] = AnsiDiffFormat

  val ansiBoth: DiffFormat[(String, String)] = AnsiDiffFormatBoth

  val text: DiffFormat[String] = TextDiffFormat

}
