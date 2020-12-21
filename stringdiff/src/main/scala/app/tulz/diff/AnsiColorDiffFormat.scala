package app.tulz.diff

import scala.Console._

object AnsiColorDiffFormat extends DiffFormat[String] {

  def apply(diff: List[DiffBlock]): String = {
    s"${RESET}${diff.map {
      case DiffBlock.Match(m)                    => s"${UNDERLINED}${m.mkString}${RESET}"
      case DiffBlock.Missing(expected)           => s"[∅|${YELLOW}${UNDERLINED}${expected.mkString}${RESET}]"
      case DiffBlock.Extra(actual)               => s"[${RED}${UNDERLINED}${actual.mkString}${RESET}|∅]"
      case DiffBlock.Different(actual, expected) => s"[${RED}${UNDERLINED}${actual.mkString}${RESET}|${YELLOW}${UNDERLINED}${expected.mkString}${RESET}]"
    }.mkString}"

  }
}
