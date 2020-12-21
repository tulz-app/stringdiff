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

  def matchingPrefixes(
    actual: List[String],
    expected: List[String]
  ): Option[(List[String], List[String], List[String])] = { // actualRem rem, expectedRem rem, prefix
    (1 to Math.min(actual.length, expected.length))
      .takeWhile(len => actual.take(len) == expected.take(len))
      .lastOption
      .map { commonPrefixLength =>
        (
          actual.drop(commonPrefixLength),
          expected.drop(commonPrefixLength),
          actual.take(commonPrefixLength)
        )
      }
  }

  def extraPrefixes(
    tokens: List[String],
    reference: List[String]
  ): List[(List[String], List[String])] = { // tokens tail, tokens prefix
    (1 to tokens.length)
      .map { len =>
        (
          len,
          tokens.take(len) != reference.take(len)
        )
      }
      .toList
      .takeWhile { case (_, keepGoing) =>
        keepGoing
      }
      .map { case (prefixLength, _) =>
        prefixLength
      }
      .map { prefixLength =>
        (
          tokens.drop(prefixLength),
          tokens.take(prefixLength)
        )
      }
  }

}
