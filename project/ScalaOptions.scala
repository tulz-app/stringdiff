import sbt.Keys._
import sbt._

object ScalaOptions {
  val fixOptions = Seq(
    scalacOptions ~= (_.filterNot(
      Set(
        "-Wdead-code",
        "-Wunused:implicits",
        "-Wunused:explicits",
        "-Wunused:imports",
        "-Wunused:params"
      )
    )),
    scalacOptions ++= CrossVersion.partialVersion(scalaVersion.value).collect { case (2, 13) =>
      "-Ymacro-annotations"
    },
    (Compile / doc / scalacOptions) ~= (_.filterNot(
      Set(
        "-scalajs",
        "-deprecation",
        "-explain-types",
        "-explain",
        "-feature",
        "-language:existentials,experimental.macros,higherKinds,implicitConversions",
        "-unchecked",
        "-Xfatal-warnings",
        "-Ykind-projector",
        "-from-tasty",
        "-encoding",
        "utf8"
      )
    ))
  )

}
