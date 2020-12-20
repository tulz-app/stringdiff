package app.tulz.diff

object Tokens {

  private val whitespace    = "\\s".r
  private val nonWhitespace = "\\S".r

  def isWhitespace(s: String): Boolean = {
    whitespace.findFirstIn(s).isDefined
  }

  def tokenize(s: String): List[String] = {
    var result = List.empty[String]
    var work   = s
    while (work.nonEmpty) {
      val whiteSpaceIndex = whitespace.findFirstMatchIn(work).fold(work.length)(_.start)
      if (whiteSpaceIndex > 0) {
        result = work.substring(0, whiteSpaceIndex) :: result
        work = work.substring(whiteSpaceIndex)
      }
      val nonWhiteSpaceIndex = nonWhitespace.findFirstMatchIn(work).fold(work.length)(_.start)
      if (nonWhiteSpaceIndex > 0) {
        result = work.substring(0, nonWhiteSpaceIndex) :: result
        work = work.substring(nonWhiteSpaceIndex)
      }
    }
    result.reverse
  }

  def matchingPrefix(
    actual: List[String],
    expected: List[String]
  ): (List[String], List[String], List[String]) = { // actualRem rem, expectedRem rem, prefix
    val commonPrefixLength =
      (1 to Math.min(actual.length, expected.length))
        .takeWhile(len => actual.take(len) == expected.take(len))
        .lastOption.getOrElse(0)
    (actual.drop(commonPrefixLength), expected.drop(commonPrefixLength), actual.take(commonPrefixLength))
  }

  def nonMatchingPrefix(
    actual: List[String],
    expected: List[String]
  ): (List[String], List[String], List[String], List[String]) = { // actualRem rem, expectedRem rem, prefix
    val maxLen = Math.min(actual.length, expected.length)
    val distinctPrefixLength =
      (1 to maxLen)
        .takeWhile { len =>
          val lastIsWhitespace = isWhitespace(actual(len - 1)) && isWhitespace(expected(len - 1))
          val nextIsDifferent  = len < maxLen && actual(len) != expected(len)
          actual(len - 1) != expected(len - 1) || (lastIsWhitespace && nextIsDifferent)
        }
        .lastOption.getOrElse(0)
    (
      actual.drop(distinctPrefixLength),
      expected.drop(distinctPrefixLength),
      actual.take(distinctPrefixLength),
      expected.take(distinctPrefixLength)
    )
  }

  def extraPrefix(
    tokens: List[String],
    reference: List[String]
  ): (List[String], List[String]) = { // tokens tail, tokens prefix
    val maxLen = Math.min(tokens.length, reference.length)
    val prefixLength =
      (1 to maxLen)
        .takeWhile { len =>
          tokens(len - 1) != reference.head
        }
        .lastOption.getOrElse(0)
    (tokens.drop(prefixLength), tokens.take(prefixLength))
  }

}
