ThisBuild / organization := "app.tulz"
ThisBuild / homepage := Some(url("https://github.com/tulz-app/stringdiff"))
ThisBuild / licenses += ("MIT", url("https://github.com/tulz-app/stringdiff/blob/main/LICENSE.md"))
ThisBuild / developers := List(
  Developer(
    id = "yurique",
    name = "Iurii Malchenko",
    email = "i@yurique.com",
    url = url("https://github.com/yurique")
  )
)
ThisBuild / releasePublishArtifactsAction := PgpKeys.publishSigned.value
ThisBuild / publishTo := sonatypePublishToBundle.value
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / sonatypeProfileName := "yurique"
ThisBuild / publishArtifact in Test := false
ThisBuild / publishMavenStyle := true
ThisBuild / releaseCrossBuild := false
ThisBuild / crossScalaVersions := Seq("2.13.4")

lazy val noPublish = Seq(
  publishLocal / skip := true,
  publish / skip := true,
  publishTo := Some(Resolver.file("Unused transient repository", file("target/unusedrepo")))
)

lazy val stringdiff =
  crossProject(JVMPlatform, JSPlatform)
    .crossType(CrossType.Pure)
    .in(file("stringdiff"))
    .settings(
      scalaVersion := "2.13.4",
      scalacOptions := Seq(
        "-unchecked",
        "-deprecation",
        "-feature",
        "-Xlint:nullary-unit,inaccessible,infer-any,missing-interpolator,private-shadow,type-parameter-shadow,poly-implicit-overload,option-implicit,delayedinit-select,stars-align",
        "-Xcheckinit",
        "-Ywarn-value-discard",
        "-language:implicitConversions",
        "-encoding",
        "utf8"
      ),
      libraryDependencies ++= Seq(
        "org.scalatest" %%% "scalatest" % "3.2.0" % Test
      ),
      description := "String diff for scala.",
      scmInfo := Some(
        ScmInfo(
          url("https://github.com/tulz-app/stringdiff"),
          "scm:git@github.com/tulz-app/stringdiff.git"
        )
      )
    )

lazy val root = project
  .in(file("."))
  .settings(noPublish)
  .settings(
    name := "app.tulz.stringdiff"
  )
  .aggregate(
    stringdiff.js,
    stringdiff.jvm
  )
