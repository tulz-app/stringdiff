package app.tulz.diff

import app.tulz.diff.format.AnsiDiffFormat
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers
import scala.Console._

class DiffTests extends AnyFunSuite with Matchers {

  import DiffElement._

  implicit class StringWithClean(s: String) {

    def clean: String = s.trim.replaceAll("\\s+", " ")

  }

  private def doTest(
    name: String,
    s1: String,
    s2: String,
    expectedDiff: List[DiffElement[String]]
  ): Unit = {
    val dashes = 80
    test(name) {
      println(s"--- ${name} ${"-" * (dashes - name.length - 5)}")
      val result = TokenDiff.diff(s1.clean, s2.clean)
      if (result != expectedDiff) {
        println(s"| ${RED_B}${BLACK} ! failed ! ${RESET}")
      }
      println(s"| str1:     ${s1.clean}")
      println(s"| str2:     ${s2.clean}")
      println(s"| diff:")
      println(s"| ${AnsiDiffFormat(result)}")
      if (result != expectedDiff) {
        println(s"| expected")
        println(s"| ${AnsiDiffFormat(expectedDiff)}")
      }
      println("-" * dashes)
      println()
      result shouldBe expectedDiff
    }
  }

  doTest(
    "equal strings",
    "diff-1 diff-2 diff-3",
    "diff-1 diff-2 diff-3",
    List(InBoth("diff-1 diff-2 diff-3"))
  )

  doTest(
    "different prefixes",
    "prefix-1 match-1 match-2 match-3",
    "prefix-2 match-1 match-2 match-3",
    List(Diff("prefix-1", "prefix-2"), InBoth(" match-1 match-2 match-3"))
  )

  doTest(
    "different suffixes",
    "match-1 match-2 match-3 suffix-1",
    "match-1 match-2 match-3 suffix-2",
    List(InBoth("match-1 match-2 match-3 "), Diff("suffix-1", "suffix-2"))
  )

  doTest(
    "different token in the middle",
    "match-1 match-2 diff-1 match-3",
    "match-1 match-2 diff-2 match-3",
    List(InBoth("match-1 match-2 "), Diff("diff-1", "diff-2"), InBoth(" match-3"))
  )

  doTest(
    "different prefix and suffix and token in the middle",
    "prefix-1 match-1 match-2 diff-1 match-3 suffix-1",
    "prefix-2 match-1 match-2 diff-2 match-3 suffix-2",
    List(
      Diff("prefix-1", "prefix-2"),
      InBoth(" match-1 match-2 "),
      Diff("diff-1", "diff-2"),
      InBoth(" match-3 "),
      Diff("suffix-1", "suffix-2")
    )
  )

  doTest(
    "extra prefix in s1",
    "prefix-1 match-1 match-2 match-3",
    "         match-1 match-2 match-3",
    List(InFirst("prefix-1 "), InBoth("match-1 match-2 match-3"))
  )

  doTest(
    "missing prefix in s1",
    "         match-1 match-2 match-3",
    "prefix-1 match-1 match-2 match-3",
    List(InSecond("prefix-1 "), InBoth("match-1 match-2 match-3"))
  )

  doTest(
    "extra token in s1",
    "match-1 diff-1 match-2",
    "match-1        match-2",
    List(InBoth("match-1"), InFirst(" diff-1"), InBoth(" match-2"))
  )

  doTest(
    "extra token in s2",
    "match-1        match-2",
    "match-1 diff-1 match-2",
    List(InBoth("match-1"), InSecond(" diff-1"), InBoth(" match-2"))
  )

  doTest(
    "two extra tokens in s1",
    "match-1 diff-1 diff-2 match-2",
    "match-1               match-2",
    List(InBoth("match-1"), InFirst(" diff-1 diff-2"), InBoth(" match-2"))
  )

  doTest(
    "two extra tokens in s2",
    "match-1               match-2",
    "match-1 diff-1 diff-2 match-2",
    List(InBoth("match-1"), InSecond(" diff-1 diff-2"), InBoth(" match-2"))
  )

  doTest(
    "extra prefix and two extra tokens in s1",
    "prefix-1 match-1 diff-1 diff-2 match-2",
    "         match-1               match-2",
    List(InFirst("prefix-1 "), InBoth("match-1 "), InFirst("diff-1 diff-2 "), InBoth("match-2"))
  )

  doTest(
    "extra prefix in s1, two extra tokens in s2",
    "prefix-1 match-1               match-2",
    "         match-1 diff-1 diff-2 match-2",
    List(InFirst("prefix-1 "), InBoth("match-1"), InSecond(" diff-1 diff-2"), InBoth(" match-2"))
  )

  doTest(
    "extra suffix, and two extra tokens in s1",
    "match-1 diff-1 diff-2 match-2 suffix-1",
    "match-1               match-2        ",
    List(InBoth("match-1"), InFirst(" diff-1 diff-2"), InBoth(" match-2"), InFirst(" suffix-1"))
  )

  doTest(
    "extra suffix in s1, two extra tokens in s2",
    "match-1               match-2 suffix-1",
    "match-1 diff-1 diff-2 match-2         ",
    List(InBoth("match-1 "), InSecond("diff-1 diff-2 "), InBoth("match-2"), InFirst(" suffix-1"))
  )

  doTest(
    "extra suffix in s1",
    "match-1 match-2 match-3 suffix-1",
    "match-1 match-2 match-3         ",
    List(InBoth("match-1 match-2 match-3"), InFirst(" suffix-1"))
  )

  doTest(
    "extra suffix in s2",
    "match-1 match-2 match-3        ",
    "match-1 match-2 match-3 suffix-1",
    List(InBoth("match-1 match-2 match-3"), InSecond(" suffix-1"))
  )

  doTest(
    "extra prefix and suffix in s1",
    "prefix-1 match-1 match-2 match-3 suffix-1",
    "         match-1 match-2 match-3         ",
    List(InFirst("prefix-1 "), InBoth("match-1 match-2 match-3"), InFirst(" suffix-1"))
  )

  doTest(
    "extra prefix in s1, extra suffix in s2",
    "prefix-1 match-1 match-2 match-3        ",
    "        match-1 match-2 match-3 suffix-1",
    List(InFirst("prefix-1 "), InBoth("match-1 match-2 match-3"), InSecond(" suffix-1"))
  )

  doTest(
    "extra prefix and suffix in s2",
    "         match-1 match-2 match-3         ",
    "prefix-1 match-1 match-2 match-3 suffix-1",
    List(InSecond("prefix-1 "), InBoth("match-1 match-2 match-3"), InSecond(" suffix-1"))
  )

  doTest(
    "extra prefix, suffix, and token in s1",
    "prefix-1 match-1 match-2 diff-1 match-3 match-4 suffix-1",
    "         match-1 match-2        match-3 match-4        ",
    List(InFirst("prefix-1 "), InBoth("match-1 match-2"), InFirst(" diff-1"), InBoth(" match-3 match-4"), InFirst(" suffix-1"))
  )

  doTest(
    "extra prefix, suffix and token in s2",
    "         match-1 match-2        match-3 match-4         ",
    "prefix-1 match-1 match-2 diff-1 match-3 match-4 suffix-1",
    List(InSecond("prefix-1 "), InBoth("match-1 match-2"), InSecond(" diff-1"), InBoth(" match-3 match-4"), InSecond(" suffix-1"))
  )

  doTest(
    "extra prefix and suffix in s2, extra token in s1",
    "         match-1 match-2 diff-1 match-3 match-4         ",
    "prefix-1 match-1 match-2        match-3 match-4 suffix-1",
    List(InSecond("prefix-1 "), InBoth("match-1 match-2"), InFirst(" diff-1"), InBoth(" match-3 match-4"), InSecond(" suffix-1"))
  )

  doTest(
    "extra prefix and suffix in s1, extra token in s2",
    "prefix-1 match-1 match-2        match-3 match-4 suffix-1",
    "         match-1 match-2 diff-1 match-3 match-4         ",
    List(InFirst("prefix-1 "), InBoth("match-1 match-2"), InSecond(" diff-1"), InBoth(" match-3 match-4"), InFirst(" suffix-1"))
  )

  doTest(
    "example 1",
    "prefix-1 match-1 match-2 diff-1         match-3 match-4        ",
    "         match-1 match-2 diff-2 diff-3 match-3 match-4 suffix-1",
    List(
      InFirst("prefix-1 "),
      InBoth("match-1 match-2 "),
      Diff("diff-1", "diff-2 diff-3"),
      InBoth(" match-3 match-4"),
      InSecond(" suffix-1")
    )
  )

  doTest(
    "example 2",
    "         match-1 match-2 diff-1 diff-2 match-3 match-4 suffix-1",
    "prefix-1 match-1 match-2 diff-3        match-3 match-4         ",
    List(
      InSecond("prefix-1 "),
      InBoth("match-1 match-2 "),
      Diff("diff-1 diff-2", "diff-3"),
      InBoth(" match-3 match-4"),
      InFirst(" suffix-1")
    )
  )

  doTest(
    "example 3",
    "prefix-1 common-1 diff-1 common-2         ",
    "         common-1 diff-2 common-2 suffix-2",
    List(InFirst("prefix-1 "), InBoth("common-1 "), Diff("diff-1", "diff-2"), InBoth(" common-2"), InSecond(" suffix-2"))
  )

  doTest(
    "bigger one",
    Seq(
      "         match-1 match-2 diff-1 diff-2 match-3 match-4 suffix-1",
      "prefix-1 match-1 match-2 diff-1         match-3 match-4         ",
      "prefix-1 match-1 match-2 diff-1 match-3 match-4 suffix-1",
      "         match-1 match-2         match-3 match-4        ",
      "         match-1 match-2 diff-1 match-3 match-4        "
    ).mkString("", " ", " separating "),
    Seq(
      "prefix-1 match-1 match-2 diff-3         match-3 match-4         ",
      "         match-1 match-2 diff-2 diff-3 match-3 match-4 suffix-1",
      "         match-1 match-2         match-3 match-4        ",
      "prefix-1 match-1 match-2 diff-1 match-3 match-4 suffix-1",
      "prefix-1 match-1 match-2         match-3 match-4 suffix-1"
    ).mkString("", " ", " separating "),
    List(
      InSecond("prefix-1 "),
      InBoth("match-1 match-2 "),
      Diff("diff-1 diff-2", "diff-3"),
      InBoth(" match-3 match-4 "),
      InFirst("suffix-1 prefix-1 "),
      InBoth("match-1 match-2 "),
      Diff("diff-1", "diff-2 diff-3"),
      InBoth(" match-3 match-4 "),
      Diff("prefix-1", "suffix-1"),
      InBoth(" match-1 match-2"),
      InFirst(" diff-1"),
      InBoth(" match-3 match-4 "),
      Diff("suffix-1", "prefix-1"),
      InBoth(" match-1 match-2"),
      InSecond(" diff-1"),
      InBoth(" match-3 match-4 "),
      InSecond("suffix-1 prefix-1 "),
      InBoth("match-1 match-2 "),
      InFirst("diff-1 "),
      InBoth("match-3 match-4 "),
      InSecond("suffix-1 "),
      InBoth("separating")
    )
  )

}
