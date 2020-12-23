package app.tulz.diff.util

import app.tulz.diff.DiffElement
import app.tulz.diff.DiffElement.InBoth
import app.tulz.diff.DiffElement.InFirst
import app.tulz.diff.DiffElement.InSecond

import scala.collection.mutable.ListBuffer
import scala.collection.IndexedSeqView

private[diff] object DiffTokenize {

  private def partitionFirstSecond(
    diff: List[DiffElement[IndexedSeqView[String]]],
    p: DiffElement[IndexedSeqView[String]] => Boolean,
    transform: (List[DiffElement[IndexedSeqView[String]]], List[DiffElement[IndexedSeqView[String]]]) => Seq[DiffElement[IndexedSeqView[String]]]
  ): List[DiffElement[IndexedSeqView[String]]] = {
    ListScan.withBuffer[DiffElement[IndexedSeqView[String]], DiffElement[IndexedSeqView[String]]](diff) { (list, buffer) =>
      val (nonFirstSecond, firstSecondAndRest) = list.span(!_.inFirstOrSecond)
      buffer.addAll(nonFirstSecond)
      val (firstSecond, rest) = firstSecondAndRest.span(_.inFirstOrSecond)
      val (first, second)     = firstSecond.partition(p)
      if (first.nonEmpty && second.nonEmpty) {
        buffer.addAll(transform(first, second))
      } else {
        buffer.addAll(first)
        buffer.addAll(second)
      }
      rest
    }
  }

  private def samePrefix(
    group1: List[DiffElement[IndexedSeqView[String]]],
    group2: List[DiffElement[IndexedSeqView[String]]]
  ): Option[
    (
      List[DiffElement[IndexedSeqView[String]]],
      List[DiffElement[IndexedSeqView[String]]],
      List[DiffElement[IndexedSeqView[String]]]
    )
  ] = {
    group1.indices
      .map(_ + 1)
      .takeWhile(i =>
        i <= group2.size &&
          ((group1(i - 1), group2(i - 1)) match {
            case (InFirst(first), InSecond(second)) => same(first, second)
            case (InSecond(second), InFirst(first)) => same(first, second)
            case _                                  => false
          })
      ).maxOption.map { samePrefixLength =>
        val prefix = group1.take(samePrefixLength).collect {
          case InFirst(first)  => InBoth(first)
          case InSecond(first) => InBoth(first)
        }
        (prefix, group1.drop(samePrefixLength), group2.drop(samePrefixLength))
      }
  }

  private def sameSuffix(
    group1: List[DiffElement[IndexedSeqView[String]]],
    group2: List[DiffElement[IndexedSeqView[String]]]
  ): Option[
    (
      List[DiffElement[IndexedSeqView[String]]],
      List[DiffElement[IndexedSeqView[String]]],
      List[DiffElement[IndexedSeqView[String]]]
    )
  ] = {
    group1.indices
      .map(_ + 1)
      .takeWhile(i =>
        i <= group2.size &&
          ((group1(group1.length - i), group2(group2.size - i)) match {
            case (InFirst(first), InSecond(second)) => same(first, second)
            case (InSecond(second), InFirst(first)) => same(first, second)
            case _                                  => false
          })
      ).maxOption.map { sameSuffixLength =>
        val suffix = group1.takeRight(sameSuffixLength).collect {
          case InFirst(first)   => InBoth(first)
          case InSecond(second) => InBoth(second)
        }
        (group1.dropRight(sameSuffixLength), group2.dropRight(sameSuffixLength), suffix)
      }
  }

  private def prefixIsSuffix(
    group1: List[DiffElement[IndexedSeqView[String]]],
    group2: List[DiffElement[IndexedSeqView[String]]]
  ): Option[
    (
      List[DiffElement[IndexedSeqView[String]]],
      List[DiffElement[IndexedSeqView[String]]],
      List[DiffElement[IndexedSeqView[String]]]
    )
  ] = {
    group1.indices
      .map(_ + 1)
      .takeWhile(i =>
        i <= group2.size &&
          group1.take(i).zip(group2.takeRight(i)).forall {
            case (InFirst(first), InSecond(second)) => same(first, second)
            case (InSecond(second), InFirst(first)) => same(first, second)
            case _                                  => false
          }
      ).maxOption.map { prefixSuffixLength =>
        val prefix = group1.take(prefixSuffixLength).collect {
          case InFirst(first)  => InBoth(first)
          case InSecond(first) => InBoth(first)
        }
        (prefix, group1.drop(prefixSuffixLength), group2.dropRight(prefixSuffixLength))
      }
  }

  private def suffixIsPrefix(
    group1: List[DiffElement[IndexedSeqView[String]]],
    group2: List[DiffElement[IndexedSeqView[String]]]
  ): Option[
    (
      List[DiffElement[IndexedSeqView[String]]],
      List[DiffElement[IndexedSeqView[String]]],
      List[DiffElement[IndexedSeqView[String]]]
    )
  ] = {
    group1.indices
      .map(_ + 1)
      .takeWhile(i =>
        i <= group2.size &&
          group1.takeRight(i).zip(group2.take(i)).forall {
            case (InFirst(first), InSecond(second)) => same(first, second)
            case (InSecond(second), InFirst(first)) => same(first, second)
            case _                                  => false
          }
      ).maxOption.map { suffixPrefixLength =>
        val suffix = group1.takeRight(suffixPrefixLength).collect {
          case InFirst(first)  => InBoth(first)
          case InSecond(first) => InBoth(first)
        }
        (group1.dropRight(suffixPrefixLength), group2.drop(suffixPrefixLength), suffix)
      }
  }

  private def processFirstSecondGroups(
    diff: List[DiffElement[IndexedSeqView[String]]],
    p: DiffElement[IndexedSeqView[String]] => Boolean
  ): List[DiffElement[IndexedSeqView[String]]] =
    partitionFirstSecond(
      diff,
      p,
      (group1, group2) => {
        val buffer       = ListBuffer.empty[DiffElement[IndexedSeqView[String]]]
        val bufferSuffix = ListBuffer.empty[DiffElement[IndexedSeqView[String]]]

        var work1 = group1
        var work2 = group2
        samePrefix(work1, work2).map { case (prefix, rest1, rest2) =>
          work1 = rest1
          work2 = rest2
          buffer.addAll(prefix)
        }
        sameSuffix(work1, work2).foreach { case (rest1, rest2, suffix) =>
          work1 = rest1
          work2 = rest2
          bufferSuffix.prependAll(suffix)
        }

        prefixIsSuffix(work1, work2)
          .map { case (prefixSuffix, rest1, rest2) =>
            buffer.addAll(rest2)
            buffer.addAll(prefixSuffix)
            buffer.addAll(rest1)
            buffer.addAll(bufferSuffix)
            ()
          }
          .orElse {
            suffixIsPrefix(work1, work2).map { case (rest1, rest2, suffixPrefix) =>
              buffer.addAll(rest1)
              buffer.addAll(suffixPrefix)
              buffer.addAll(rest2)
              buffer.addAll(bufferSuffix)
              ()
            }
          }
          .getOrElse {
            buffer.addAll(work1)
            buffer.addAll(work2)
            buffer.addAll(bufferSuffix)
            ()
          }

        buffer.toList
      }
    )

  def firstsGoFirst(
    diff: List[DiffElement[IndexedSeqView[String]]]
  ): List[DiffElement[IndexedSeqView[String]]] =
    processFirstSecondGroups(diff, _.inFirst)

  def secondsGoFirst(
    diff: List[DiffElement[IndexedSeqView[String]]]
  ): List[DiffElement[IndexedSeqView[String]]] =
    processFirstSecondGroups(diff, _.inSecond)

  private def same(s1: IndexedSeqView[String], s2: IndexedSeqView[String]): Boolean =
    s1.size == s2.size && s1.indices.forall(i => s1(i) == s2(i))

  def join(diff: List[DiffElement[IndexedSeqView[String]]]): List[DiffElement[IndexedSeqView[String]]] =
    ListScan(diff) {
      case InSecond(second) :: InFirst(first) :: tail if same(first, second) =>
        Nil -> (
          InBoth(first) :: tail
        )

      case InFirst(first) :: InSecond(second) :: tail if same(first, second) =>
        Nil -> (
          InBoth(first) :: tail
        )

      case head :: tail =>
        (head :: Nil) -> tail

      case Nil => Nil -> Nil
    }

  def moveWhitespace(
    diff: List[DiffElement[IndexedSeqView[String]]]
  ): List[DiffElement[IndexedSeqView[String]]] =
    ListScan(diff) {

      case InSecond(second) :: InBoth(Whitespace(both)) :: InFirst(first) :: tail if first == second =>
        Nil -> (
          InFirst(both) :: InBoth(second) :: InSecond(both) :: tail
        )

      case InFirst(first) :: InBoth(Whitespace(both)) :: InSecond(second) :: tail if first == second =>
        Nil -> (
          InSecond(both) :: InBoth(second) :: InFirst(both) :: tail
        )

      case InSecond(second) :: InBoth(Whitespace(both)) :: InFirst(first) :: tail =>
        Nil -> (
          InFirst(both) :: InFirst(first) :: InSecond(second) :: InSecond(both) :: tail
        )

      case InFirst(first) :: InBoth(Whitespace(both)) :: InSecond(second) :: tail =>
        Nil -> (
          InFirst(first) :: InFirst(both) :: InSecond(both) :: InSecond(second) :: tail
        )

      case InFirst(first1) :: InBoth(Whitespace(both)) :: InFirst(first2) :: tail =>
        Nil -> (
          InFirst(first1) :: InFirst(both) :: InFirst(first2) :: InSecond(both) :: tail
        )

      case InSecond(second1) :: InBoth(Whitespace(both)) :: InSecond(second2) :: tail =>
        Nil -> (
          InFirst(both) :: InSecond(second1) :: InSecond(both) :: InSecond(second2) :: tail
        )

      case head :: tail =>
        (head :: Nil) -> tail

      case Nil => Nil -> Nil
    }

  private val whitespace = "\\s+".r
  private object Whitespace {
    def unapply(s: IndexedSeqView[String]): Option[IndexedSeqView[String]] = {
      Some(s).filter(_.forall(whitespace.matches))
    }
  }

}
