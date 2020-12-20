package app.tulz.diff

import scala.Console._

class StringDiff(
  beforeAll: String = RESET,
  beforeNoMatch: String = "[",
  beforeExpected: String = YELLOW,
  afterExpected: String = RESET,
  between: String = "|",
  beforeActual: String = RED,
  afterActual: String = RESET,
  afterNoMatch: String = "]",
  empty: String = "∅",
  beforeMatch: String = "",
  afterMatch: String = "",
  afterAll: String = ""
) {

  import Tokens._

  def apply(
    actual: String,
    expected: String
  ): String = {
//    println(s"actual: ${tokenize(actual)}")
//    println(s"expected: ${tokenize(expected)}")
    val options = diff(tokenize(actual), tokenize(expected), List.empty)
    if (options.isEmpty) {
      "no diff result (probably a bug in app.tulz.stringdiff)"
    } else {
//      println("options:")
//      options.foreach { s =>
//        println(diffToString(s))
//      }
      val withMatchLength = options.map { path =>
        val matchLengths = path.collect { case DiffBlock.Match(s) => s.length }
        path -> matchLengths.sum
      }
      val largestMatch     = withMatchLength.map(_._2).max
      val withLargestMatch = withMatchLength.filter(_._2 == largestMatch).map(_._1)
      val best             = withLargestMatch.map(diffToString).minBy(_.length)
//      println("best:")
//      println(best)

      best
    }
  }

  private def diff(
    actual: List[String],
    expected: List[String],
    path: List[DiffBlock]
  ): List[List[DiffBlock]] = {
    tryEnd(actual, expected, path) ++
      tryCommonPrefix(actual, expected, path) ++
      tryExtraPrefixInActual(actual, expected, path) ++
      tryExtraPrefixInExpected(actual, expected, path) ++
      tryNonMatchingPrefix(actual, expected, path)
  }

  private def tryEnd(
    actual: List[String],
    expected: List[String],
    path: List[DiffBlock]
  ): List[List[DiffBlock]] = {
    if (actual.isEmpty || actual.isEmpty != expected.isEmpty) {
      List(
        DiffBlock.NoMatch(actual, expected) :: path
      )
    } else if (actual.isEmpty || expected.isEmpty) {
      throw new RuntimeException("looks like you have hit a bug in app.tulz.stringdiff")
    } else {
      List.empty
    }
  }

  private def tryExtraPrefixInActual(
    actual: List[String],
    expected: List[String],
    path: List[DiffBlock]
  ): List[List[DiffBlock]] = {
    val (actualTail, actualPrefix) = extraPrefix(actual, expected)
    if (actualPrefix.isEmpty) {
      List.empty
    } else {
      diff(
        actualTail,
        expected,
        DiffBlock.NoMatch(actualPrefix, List.empty) :: path
      )
    }
  }

  private def tryExtraPrefixInExpected(
    actual: List[String],
    expected: List[String],
    path: List[DiffBlock]
  ): List[List[DiffBlock]] = {
    val (expectedTail, expectedPrefix) = extraPrefix(expected, actual)
    if (expectedPrefix.isEmpty) {
      List.empty
    } else {
      diff(
        actual,
        expectedTail,
        DiffBlock.NoMatch(List.empty, expectedPrefix) :: path
      )
    }
  }

  private def tryCommonPrefix(
    actual: List[String],
    expected: List[String],
    path: List[DiffBlock]
  ): List[List[DiffBlock]] = {
    if (actual.isEmpty || expected.isEmpty) {
      List.empty
    } else {
      val (actualTail, expectedTail, prefix) = matchingPrefix(actual, expected)
      if (prefix.isEmpty) {
        List.empty
      } else {
        diff(
          actualTail,
          expectedTail,
          DiffBlock.Match(prefix) :: path
        )
      }
    }
  }

  private def tryNonMatchingPrefix(
    actual: List[String],
    expected: List[String],
    path: List[DiffBlock]
  ): List[List[DiffBlock]] = {
    val (actualTail, expectedTail, actualPrefix, expectedPrefix) = nonMatchingPrefix(actual, expected)
    if (actualPrefix.isEmpty) {
      List.empty
    } else {
      diff(
        actualTail,
        expectedTail,
        DiffBlock.NoMatch(actualPrefix, expectedPrefix) :: path
      )
    }
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
          if (actual.nonEmpty || expected.nonEmpty) {
            buffer.append(beforeNoMatch)
            buffer.append(beforeExpected)
            if (expected.nonEmpty) {
              expected.foreach(buffer.append)
            } else {
              buffer.append(empty)
            }
            buffer.append(afterExpected)
            buffer.append(between)
            buffer.append(beforeActual)
            if (actual.nonEmpty) {
              actual.foreach(buffer.append)
            } else {
              buffer.append(empty)
            }
            buffer.append(afterActual)
            buffer.append(afterNoMatch)
          }
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
    empty = "<empty/>",
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
