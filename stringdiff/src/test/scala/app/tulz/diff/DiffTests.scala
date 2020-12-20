package app.tulz.diff

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers

class DiffTests extends AnyFunSuite with Matchers {

  private val diff = StringDiff.xml

  //  test("print examples") {
  //    examples
  //      .flatMap { case (actualTest, expectedTest) =>
  //        if (actualTest.length != expectedTest.length) {
  //          Seq(
  //            (actualTest, expectedTest),
  //            (expectedTest, actualTest)
  //          ).distinct
  //        } else {
  //          Seq(
  //            (actualTest, expectedTest)
  //          )
  //        }
  //      }.foreach { case (actualTest, expectedTest) =>
  //        println(s"'${actualTest}'")
  //        println(s"'${expectedTest}'")
  //        println()
  //        println(StringDiff(actualTest, expectedTest))
  //        println("-" * 50)
  //
  //      }
  //  }

  implicit class StringWithClean(s: String) {

    def clean: String = s.trim.replaceAll("\\s+", " ")

  }

  private def doTest(
    name: String,
    act: String,
    exp: String,
    expectedDiff: String
  ): Unit = {
    test(name) {
      val result = diff(act.clean, exp.clean)

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
        println("---" * 30)
      }
      result shouldBe expectedDiff
    }
  }

  doTest(
    "equal strings",
    "token1 token2 token3",
    "token1 token2 token3",
    "<diff><match>token1 token2 token3</match></diff>"
  )

  doTest(
    "different prefixes",
    "prefix1 match1 match2 match3",
    "prefix2 match1 match2 match3",
    "<diff><no-match><expected>prefix2</expected><actual>prefix1</actual></no-match><match> match1 match2 match3</match></diff>"
  )

  doTest(
    "different suffixes",
    "match1 match2 match3 suffix1",
    "match1 match2 match3 suffix2",
    "<diff><match>match1 match2 match3 </match><no-match><expected>suffix2</expected><actual>suffix1</actual></no-match></diff>"
  )

  doTest(
    "different token in the middle",
    "match1 match2 inside1 match3",
    "match1 match2 inside2 match3",
    "<diff><match>match1 match2 </match><no-match><expected>inside2</expected><actual>inside1</actual></no-match><match> match3</match></diff>"
  )

  doTest(
    "different prefix and suffix and token in the middle",
    "prefix1 match1 match2 inside1 match3 suffix1",
    "prefix2 match1 match2 inside2 match3 suffix2",
    "<diff><no-match><expected>prefix2</expected><actual>prefix1</actual></no-match><match> match1 match2 </match><no-match><expected>inside2</expected><actual>inside1</actual></no-match><match> match3 </match><no-match><expected>suffix2</expected><actual>suffix1</actual></no-match></diff>"
  )

  doTest(
    "extra prefix in actual",
    "prefix1 match1 match2 match3",
    "match1 match2 match3        ",
    "<diff><no-match><expected><empty/></expected><actual>prefix1 </actual></no-match><match>match1 match2 match3</match></diff>"
  )

  doTest(
    "missing prefix in actual",
    "match1 match2 match3        ",
    "prefix1 match1 match2 match3",
    "<diff><no-match><expected>prefix1 </expected><actual><empty/></actual></no-match><match>match1 match2 match3</match></diff>"
  )

  doTest(
    "extra suffix in actual",
    "match1 match2 match3 suffix1",
    "match1 match2 match3        ",
    "<diff><match>match1 match2 match3</match><no-match><expected><empty/></expected><actual> suffix1</actual></no-match></diff>"
  )

  doTest(
    "missing suffix in actual",
    "match1 match2 match3        ",
    "match1 match2 match3 suffix1",
    "<diff><match>match1 match2 match3</match><no-match><expected> suffix1</expected><actual><empty/></actual></no-match></diff>"
  )

  doTest(
    "extra prefix and suffix in actual",
    "prefix1 match1 match2 match3 suffix1",
    "        match1 match2 match3        ",
    "<diff><no-match><expected><empty/></expected><actual>prefix1 </actual></no-match><match>match1 match2 match3</match><no-match><expected><empty/></expected><actual> suffix1</actual></no-match></diff>"
  )

  doTest(
    "extra prefix in actual, extra suffix in expected",
    "prefix1 match1 match2 match3 suffix1",
    "        match1 match2 match3        ",
    "<diff><no-match><expected><empty/></expected><actual>prefix1 </actual></no-match><match>match1 match2 match3</match><no-match><expected><empty/></expected><actual> suffix1</actual></no-match></diff>"
  )

  doTest(
    "missing prefix and suffix in actual",
    "        match1 match2 match3",
    "prefix1 match1 match2 match3 suffix1",
    "<diff><no-match><expected>prefix1 </expected><actual><empty/></actual></no-match><match>match1 match2 match3</match><no-match><expected> suffix1</expected><actual><empty/></actual></no-match></diff>"
  )

  doTest(
    "missing prefix and suffix, extra token in actual",
    "prefix1 match1 match2 inside1 match3 match4 suffix1",
    "        match1 match2         match3 match4        ",
    "<diff><no-match><expected><empty/></expected><actual>prefix1 </actual></no-match><match>match1 match2 </match><no-match><expected><empty/></expected><actual>inside1 </actual></no-match><match>match3 match4</match><no-match><expected><empty/></expected><actual> suffix1</actual></no-match></diff>"
  )

  doTest(
    "missing prefix and suffix, extra token in expected",
    "        match1 match2         match3 match4        ",
    "prefix1 match1 match2 inside1 match3 match4 suffix1",
    "<diff><no-match><expected>prefix1 </expected><actual><empty/></actual></no-match><match>match1 match2 </match><no-match><expected>inside1 </expected><actual><empty/></actual></no-match><match>match3 match4</match><no-match><expected> suffix1</expected><actual><empty/></actual></no-match></diff>"
  )

  doTest(
    "missing prefix and suffix in actual, extra token in expected",
    "        match1 match2 inside1 match3 match4        ",
    "prefix1 match1 match2         match3 match4 suffix1",
    "<diff><no-match><expected>prefix1 </expected><actual><empty/></actual></no-match><match>match1 match2 </match><no-match><expected><empty/></expected><actual>inside1 </actual></no-match><match>match3 match4</match><no-match><expected> suffix1</expected><actual><empty/></actual></no-match></diff>"
  )

  doTest(
    "missing prefix and suffix in expected, extra token in actual",
    "prefix1 match1 match2         match3 match4 suffix1",
    "        match1 match2 inside1 match3 match4        ",
    "<diff><no-match><expected><empty/></expected><actual>prefix1 </actual></no-match><match>match1 match2 </match><no-match><expected>inside1 </expected><actual><empty/></actual></no-match><match>match3 match4</match><no-match><expected><empty/></expected><actual> suffix1</actual></no-match></diff>"
  )

}
