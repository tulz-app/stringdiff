package app.tulz.diff

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class DiffTests extends AnyFunSuite with Matchers {

  import DiffBlock._

  implicit class StringWithClean(s: String) {

    def clean: String = s.trim.replaceAll("\\s+", " ")

  }

  private def doTest(
    name: String,
    act: String,
    exp: String,
    expectedDiff: List[DiffBlock]
  ): Unit = {
    test(name) {
      val result = StringDiff.raw(act.clean, exp.clean)
      if (result != expectedDiff) {
        println()
        println("---" * 30)
        println("failed diff")
        println("actual:")
        println(act.clean)
        println("expected:")
        println(exp.clean)
        println("diff:")
        println(StringDiff(act.clean, exp.clean))
        println("expected:")
        println(AnsiColorDiffFormat(expectedDiff))
        println("---" * 30)
      }
      result shouldBe expectedDiff
    }
  }

  doTest(
    "equal strings",
    "token1 token2 token3",
    "token1 token2 token3",
    List(Match(List("token1", " ", "token2", " ", "token3")))
  )

  doTest(
    "different prefixes",
    "prefix1 match1 match2 match3",
    "prefix2 match1 match2 match3",
    List(Different(List("prefix1", " "), List("prefix2", " ")), Match(List("match1", " ", "match2", " ", "match3")))
  )

  doTest(
    "different suffixes",
    "match1 match2 match3 suffix1",
    "match1 match2 match3 suffix2",
    List(Match(List("match1", " ", "match2", " ", "match3", " ")), Extra(List("suffix1")))
  )

  doTest(
    "different token in the middle",
    "match1 match2 inside1 match3",
    "match1 match2 inside2 match3",
    List(Match(List("match1", " ", "match2", " ")), Different(List("inside1", " "), List("inside2", " ")), Match(List("match3")))
  )

  doTest(
    "different prefix and suffix and token in the middle",
    "prefix1 match1 match2 inside1 match3 suffix1",
    "prefix2 match1 match2 inside2 match3 suffix2",
    List(
      Different(List("prefix1", " "), List("prefix2", " ")),
      Match(List("match1", " ", "match2", " ")),
      Different(List("inside1", " "), List("inside2", " ")),
      Match(List("match3", " ")),
      Extra(List("suffix1"))
    )
  )

  doTest(
    "extra prefix in actual",
    "prefix1 match1 match2 match3",
    "match1 match2 match3        ",
    List(Extra(List("prefix1", " ")), Match(List("match1", " ", "match2", " ", "match3")))
  )

  doTest(
    "missing prefix in actual",
    "match1 match2 match3        ",
    "prefix1 match1 match2 match3",
    List(Missing(List("prefix1", " ")), Match(List("match1", " ", "match2", " ", "match3")))
  )

  doTest(
    "extra suffix in actual",
    "match1 match2 match3 suffix1",
    "match1 match2 match3        ",
    List(Match(List("match1", " ", "match2", " ", "match3")))
  )

  doTest(
    "missing suffix in actual",
    "match1 match2 match3        ",
    "match1 match2 match3 suffix1",
    List(Match(List("match1", " ", "match2", " ", "match3")))
  )

  doTest(
    "extra prefix and suffix in actual",
    "prefix1 match1 match2 match3 suffix1",
    "        match1 match2 match3        ",
    List(Extra(List("prefix1", " ")), Match(List("match1", " ", "match2", " ", "match3")))
  )

  doTest(
    "extra prefix in actual, extra suffix in expected",
    "prefix1 match1 match2 match3 suffix1",
    "        match1 match2 match3        ",
    List(Extra(List("prefix1", " ")), Match(List("match1", " ", "match2", " ", "match3")))
  )

  doTest(
    "missing prefix and suffix in actual",
    "        match1 match2 match3",
    "prefix1 match1 match2 match3 suffix1",
    List(Missing(List("prefix1", " ")), Match(List("match1", " ", "match2", " ", "match3")))
  )

  doTest(
    "missing prefix and suffix, extra token in actual",
    "prefix1 match1 match2 inside1 match3 match4 suffix1",
    "        match1 match2         match3 match4        ",
    List(Extra(List("prefix1", " ")), Match(List("match1", " ", "match2", " ")), Extra(List("inside1", " ")), Match(List("match3", " ", "match4")))
  )

  doTest(
    "missing prefix and suffix, extra token in expected",
    "        match1 match2         match3 match4        ",
    "prefix1 match1 match2 inside1 match3 match4 suffix1",
    List(Missing(List("prefix1", " ")), Match(List("match1", " ", "match2", " ")), Missing(List("inside1", " ")), Match(List("match3", " ", "match4")))
  )

  doTest(
    "missing prefix and suffix in actual, extra token in expected",
    "        match1 match2 inside1 match3 match4        ",
    "prefix1 match1 match2         match3 match4 suffix1",
    List(Missing(List("prefix1", " ")), Match(List("match1", " ", "match2", " ")), Extra(List("inside1", " ")), Match(List("match3", " ", "match4")))
  )

  doTest(
    "missing prefix and suffix in expected, extra token in actual",
    "prefix1 match1 match2         match3 match4 suffix1",
    "        match1 match2 inside1 match3 match4        ",
    List(Extra(List("prefix1", " ")), Match(List("match1", " ", "match2", " ")), Missing(List("inside1", " ")), Match(List("match3", " ", "match4")))
  )

  doTest(
    "example 1",
    "prefix common1 common2 inside1 common3 common4",
    "common1 common2 inside2 inside3 common3 suffix",
    List(
      Extra(List("prefix", " ")),
      Match(List("common1", " ", "common2", " ")),
      Different(List("inside1", " "), List("inside2", " ", "inside3", " ")),
      Match(List("common3", " ")),
      Missing(List("suffix"))
    )
  )

  doTest(
    "example 2",
    "common1 common2 inside1 inside2 common3 common4 suffix",
    "prefix common1 common2 inside3 common3",
    List(
      Missing(List("prefix", " ")),
      Match(List("common1", " ", "common2", " ")),
      Different(List("inside1", " ", "inside2", " "), List("inside3", " ")),
      Match(List("common3"))
    )
  )

}
