package app.tulz.diff

import scala.Console._

class StringDiff(
  beforeAll: String = RESET,
  beforeNoMatch: String = "[",
  beforeExpected: String = YELLOW + UNDERLINED,
  afterExpected: String = RESET,
  between: String = "|",
  beforeActual: String = RED + UNDERLINED,
  afterActual: String = RESET,
  afterNoMatch: String = "]",
  beforeExtra: String = "[" + RED + UNDERLINED,
  afterExtra: String = RESET + "|∅]",
  beforeMissing: String = "[∅|" + YELLOW + UNDERLINED,
  afterMissing: String = RESET + "]",
  beforeMatch: String = UNDERLINED,
  afterMatch: String = RESET,
  afterAll: String = ""
) {

  import Tokens._

  private var prefix = ""

  private def enter[T](label: String, f: => T): T = {
    println(s"$prefix<$label>")
    prefix = prefix + "    "
    val result = f
    prefix = prefix.substring(0, prefix.length - 4)
    println(s"$prefix</$label>")
    result
  }

  private def log(s: => String): Unit = {
    println(s"$prefix<m>")
    println(s"$prefix    $s")
    println(s"$prefix</m>")
  }

  def apply(
    actual: String,
    expected: String
  ): String = {
    val actualTokens   = tokenize(actual)
    val expectedTokens = tokenize(expected)
    val options        = diff(actualTokens, expectedTokens, List.empty)
    if (options.isEmpty) {
      "no diff result (probably a bug in app.tulz.stringdiff)"
    } else {
      println("diff for:")
      println(s"'${actual}'")
      println(s"'${expected}'")
      println("options:")
      options.foreach { s =>
        println(diffToString(s))
      }
      val collapsed = options.map(collapse).distinct
      val withMatchLength = collapsed.map { path =>
        val matchLengths = path.collect { case DiffBlock.Match(s) => s.length }
        path -> matchLengths.sum
      }
      val largestMatch     = withMatchLength.map(_._2).max
      val withLargestMatch = withMatchLength.filter(_._2 == largestMatch).map(_._1)
      withLargestMatch.map(diffToString).minBy(_.replaceAll("\\s+", "").length)
    }
  }

  private def diff(
    actual: List[String],
    expected: List[String],
    path: List[DiffBlock]
  ): List[List[DiffBlock]] = {
    enter(
      "diff", {
        log(s"'${actual.mkString}'")
        log(s"'${expected.mkString}'")
        enter("tryEnd", tryEnd(actual, expected, path)) ++
          enter("tryMatchingPrefix", tryMatchingPrefix(actual, expected, path)) ++
          enter("tryExtraPrefix", tryExtraPrefix(actual, expected, path)) ++
          enter("tryMissingPrefix", tryMissingPrefix(actual, expected, path))
      }
    )
  }

  private def tryEnd(
    actual: List[String],
    expected: List[String],
    path: List[DiffBlock]
  ): List[List[DiffBlock]] = {
    log(s"'${actual.mkString}'")
    log(s"'${expected.mkString}'")

    if (actual.isEmpty && expected.isEmpty) {
      log("--yes--")
      List(path)
    } else {
      log("--no--")
      List.empty
    }
  }

  private def tryExtraPrefix(
    actual: List[String],
    expected: List[String],
    path: List[DiffBlock]
  ): List[List[DiffBlock]] = {
    log(s"'${actual.mkString}'")
    log(s"'${expected.mkString}'")
    extraPrefixes(actual, expected).flatMap { case (actualTail, actualPrefix) =>
      log("--yes--")
      log(s"'${actualPrefix.mkString}'")
      val newPath = DiffBlock.Extra(actualPrefix) :: path
      enter("tryEnd", tryEnd(actualTail, expected, path)) ++
        enter("tryMatchingPrefix", tryMatchingPrefix(actualTail, expected, newPath)) ++
        enter("tryMissingPrefix", tryMissingPrefix(actualTail, expected, newPath))
    }
  }

  private def tryMissingPrefix(
    actual: List[String],
    expected: List[String],
    path: List[DiffBlock]
  ): List[List[DiffBlock]] = {
    log(s"'${actual.mkString}'")
    log(s"'${expected.mkString}'")

    extraPrefixes(expected, actual).flatMap { case (expectedTail, expectedPrefix) =>
      log("--yes--")
      log(s"'${expectedPrefix.mkString}'")
      val newPath = DiffBlock.Missing(expectedPrefix) :: path
      enter("tryEnd", tryEnd(actual, expectedTail, path)) ++
        enter("tryMatchingPrefix", tryMatchingPrefix(actual, expectedTail, newPath)) ++
        enter("tryExtraPrefix", tryExtraPrefix(actual, expectedTail, newPath))
    }
  }

  private def tryMatchingPrefix(
    actual: List[String],
    expected: List[String],
    path: List[DiffBlock]
  ): List[List[DiffBlock]] = {
    log(s"'${actual.mkString}'")
    log(s"'${expected.mkString}'")
    if (actual.isEmpty || expected.isEmpty) {
      log("--no--")
      List.empty
    } else {
      val (actualTail, expectedTail, prefix) = matchingPrefixes(actual, expected)
      if (prefix.isEmpty) {
        log("--no--")
        List.empty
      } else {
        log("--yes--")
        log(s"'${prefix.mkString}'")
        val newPath = DiffBlock.Match(prefix) :: path
        enter("tryEnd", tryEnd(actualTail, expectedTail, newPath)) ++
          enter("tryExtraPrefix", tryExtraPrefix(actualTail, expectedTail, newPath)) ++
          enter("tryMissingPrefix", tryMissingPrefix(actualTail, expectedTail, newPath))

      }
    }
  }

  private def collapse(diff: List[DiffBlock]): List[DiffBlock] = diff match {
    case Nil                                                            => Nil
    case DiffBlock.Missing(expected) :: DiffBlock.Extra(actual) :: tail => DiffBlock.NoMatch(actual, expected) :: collapse(tail)
    case DiffBlock.Extra(actual) :: DiffBlock.Missing(expected) :: tail => DiffBlock.NoMatch(actual, expected) :: collapse(tail)
    case head :: tail                                                   => head :: collapse(tail)
  }

  private def diffToString(diff: List[DiffBlock]): String = {
    Seq(
      beforeAll,
      diff.reverse.map {
        case DiffBlock.Match(s) =>
          val buffer = new StringBuffer
          buffer.append(beforeMatch)
          buffer.append(s.mkString)
          buffer.append(afterMatch)
          buffer.toString
        case DiffBlock.NoMatch(actual, expected) =>
          val buffer = new StringBuffer
          buffer.append(beforeNoMatch)
          buffer.append(beforeExpected)
          expected.foreach(buffer.append)
          buffer.append(afterExpected)
          buffer.append(between)
          buffer.append(beforeActual)
          actual.foreach(buffer.append)
          buffer.append(afterActual)
          buffer.append(afterNoMatch)
          buffer.toString
        case DiffBlock.Extra(actual) =>
          val buffer = new StringBuffer
          buffer.append(beforeExtra)
          actual.foreach(buffer.append)
          buffer.append(afterExtra)
          buffer.toString
        case DiffBlock.Missing(expected) =>
          val buffer = new StringBuffer
          buffer.append(beforeMissing)
          expected.foreach(buffer.append)
          buffer.append(afterMissing)
          buffer.toString
      }.mkString,
      afterAll
    ).mkString
  }

}

object StringDiff {

  val xml = new StringDiff(
    beforeAll = "<diff>",
    beforeNoMatch = "<no-match>",
    beforeExpected = "<expected>",
    afterExpected = "</expected>",
    between = "",
    beforeActual = "<actual>",
    afterActual = "</actual>",
    beforeExtra = "<extra>",
    afterExtra = "</extra>",
    beforeMissing = "<missing>",
    afterMissing = "</missing>",
    afterNoMatch = "</no-match>",
    beforeMatch = "<match>",
    afterMatch = "</match>",
    afterAll = "</diff>"
  )
  val default = new StringDiff()

  def apply(
    actual: String,
    expected: String
  ): String = default(actual, expected)

}
