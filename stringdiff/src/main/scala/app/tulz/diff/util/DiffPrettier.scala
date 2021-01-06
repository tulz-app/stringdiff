package app.tulz.diff.util

import app.tulz.diff.DiffElement
import app.tulz.diff.DiffElement.Diff
import app.tulz.diff.DiffElement.InBoth
import app.tulz.diff.compat._
import scala.collection.compat._

private[diff] object DiffPrettier {

  def apply(
    diff: List[DiffElement[String]]
  ): List[DiffElement[String]] =
    collapseInBoth(
      ListScan(diff) {
        case (d @ Diff(first, second)) :: tail =>
          val (prefix, firstRest1, secondRest1) = sameWhitespacePrefix(first, second)
          val (firstRest2, secondRest2, suffix) = sameWhitespaceSuffix(firstRest1, secondRest1)
          if (prefix.nonEmpty && suffix.nonEmpty) {
            (InBoth(prefix) :: Diff(firstRest2, secondRest2) :: InBoth(suffix) :: Nil) -> tail
          } else if (prefix.nonEmpty) {
            (InBoth(prefix) :: Diff(firstRest2, secondRest2) :: Nil) -> tail
          } else if (suffix.nonEmpty) {
            (Diff(firstRest2, secondRest2) :: InBoth(suffix) :: Nil) -> tail
          } else {
            (d :: Nil) -> tail
          }

        case head :: tail =>
          (head :: Nil) -> tail

        case Nil => Nil -> Nil
      }
    )

  private def collapseInBoth(
    diff: List[DiffElement[String]]
  ): List[DiffElement[String]] =
    ListScan(diff) {
      case InBoth(first) :: InBoth(second) :: tail =>
        Nil -> (InBoth(first + second) :: tail)

      case head :: tail =>
        (head :: Nil) -> tail

      case Nil => Nil -> Nil
    }

  private def sameWhitespacePrefix(
    s1: String,
    s2: String
  ): (String, String, String) = {
    val samePrefixLength = s1.indices
      .takeWhile(i => i < s2.length && s1.charAt(i) == s2.charAt(i) && s1.charAt(i).isWhitespace)
      .maxOption.fold(0)(_ + 1)

    (s1.take(samePrefixLength), s1.drop(samePrefixLength), s2.drop(samePrefixLength))
  }

  private def sameWhitespaceSuffix(
    s1: String,
    s2: String
  ): (String, String, String) = {
    val sameSuffixLength = s1.indices
      .takeWhile(i => i < s2.length && s1.charAt(s1.length - 1 - i) == s2.charAt(s2.length - 1 - i) && s1.charAt(i).isWhitespace)
      .maxOption.fold(0)(_ + 1)
    (s1.dropRight(sameSuffixLength), s2.dropRight(sameSuffixLength), s1.takeRight(sameSuffixLength))
  }

  private val whitespace = "\\s+".r
  private object Whitespace {
    def unapply(s: String): Option[String] = {
      Some(s).filter(whitespace.matches)
    }
  }

}
