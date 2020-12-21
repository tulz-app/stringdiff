package app.tulz.diff

object TextDiffFormat extends DiffFormat[String] {

  def apply(diff: List[DiffBlock]): String = {
    diff.map {
      case DiffBlock.Match(m)                    => m.mkString
      case DiffBlock.Missing(expected)           => s"[∅|${expected.mkString}]"
      case DiffBlock.Extra(actual)               => s"[${actual.mkString}|∅]"
      case DiffBlock.Different(actual, expected) => s"[${actual.mkString}|${expected.mkString}]"
    }.mkString
  }

}
