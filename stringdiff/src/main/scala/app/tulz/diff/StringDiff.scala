package app.tulz.diff

object StringDiff {

  import Tokens._

  def apply(
    actual: String,
    expected: String
  ): String = withFormat(actual, expected)(AnsiColorDiffFormat)

  def text(
    actual: String,
    expected: String
  ): String = withFormat(actual, expected)(TextDiffFormat)

  def raw(
    actual: String,
    expected: String
  ): List[DiffBlock] = withFormat(actual, expected)(RawDiffFormat)

  def withFormat[T](
    actual: String,
    expected: String
  )(diffFormat: DiffFormat[T]): T = {
    val actualTokens   = tokenize(actual)
    val expectedTokens = tokenize(expected)
    val options        = diff(actualTokens, expectedTokens, List.empty)
    if (options.isEmpty) {
      diffFormat(DiffBlock.Match("no diff result - this is probably a bug in app.tulz.stringdiff" :: Nil) :: Nil)
    } else {
      val collapsed = options.map(collapse).distinct
      val withMatchLength = collapsed.map { path =>
        val matchLengths = path.collect { case DiffBlock.Match(s) => s.length }
        path -> matchLengths.sum
      }
      val largestMatch     = withMatchLength.map(_._2).max
      val withLargestMatch = withMatchLength.filter(_._2 == largestMatch).map(_._1)
      diffFormat {
        withLargestMatch
          .minBy(diff => TextDiffFormat(diff).replaceAll("\\s+", "").length).reverse
      }
    }
  }

  private def diff(
    actual: List[String],
    expected: List[String],
    path: List[DiffBlock]
  ): List[List[DiffBlock]] = {
    tryEnd(actual, expected, path) ++
      tryMatchingPrefix(actual, expected, path) ++
      tryExtraPrefix(actual, expected, path) ++
      tryMissingPrefix(actual, expected, path)

  }

  private def tryEnd(
    actual: List[String],
    expected: List[String],
    path: List[DiffBlock]
  ): List[List[DiffBlock]] = {
    if (actual.isEmpty && expected.isEmpty) {
      List(path)
    } else {
      List.empty
    }
  }

  private def tryExtraPrefix(
    actual: List[String],
    expected: List[String],
    path: List[DiffBlock]
  ): List[List[DiffBlock]] = {
    extraPrefixes(actual, expected).flatMap { case (actualTail, actualPrefix) =>
      val newPath = DiffBlock.Extra(actualPrefix) :: path
      tryEnd(actualTail, expected, path) ++
        tryMatchingPrefix(actualTail, expected, newPath) ++
        tryMissingPrefix(actualTail, expected, newPath)
    }
  }

  private def tryMissingPrefix(
    actual: List[String],
    expected: List[String],
    path: List[DiffBlock]
  ): List[List[DiffBlock]] = {
    extraPrefixes(expected, actual).flatMap { case (expectedTail, expectedPrefix) =>
      val newPath = DiffBlock.Missing(expectedPrefix) :: path
      tryEnd(actual, expectedTail, path) ++
        tryMatchingPrefix(actual, expectedTail, newPath) ++
        tryExtraPrefix(actual, expectedTail, newPath)
    }
  }

  private def tryMatchingPrefix(
    actual: List[String],
    expected: List[String],
    path: List[DiffBlock]
  ): List[List[DiffBlock]] = {
    matchingPrefixes(actual, expected).toList.flatMap { case (actualTail, expectedTail, prefix) =>
      val newPath = DiffBlock.Match(prefix) :: path
      tryEnd(actualTail, expectedTail, newPath) ++
        tryExtraPrefix(actualTail, expectedTail, newPath) ++
        tryMissingPrefix(actualTail, expectedTail, newPath)
    }
  }

  private def collapse(diff: List[DiffBlock]): List[DiffBlock] = diff match {
    case Nil                                                            => Nil
    case DiffBlock.Missing(expected) :: DiffBlock.Extra(actual) :: tail => DiffBlock.Different(actual, expected) :: collapse(tail)
    case DiffBlock.Extra(actual) :: DiffBlock.Missing(expected) :: tail => DiffBlock.Different(actual, expected) :: collapse(tail)
    case head :: tail                                                   => head :: collapse(tail)
  }
}
