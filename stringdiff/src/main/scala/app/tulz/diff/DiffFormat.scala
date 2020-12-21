package app.tulz.diff

trait DiffFormat[Out] extends (List[DiffBlock] => Out)

object DiffFormat {

  val ansi: DiffFormat[String] = AnsiColorDiffFormat

}
