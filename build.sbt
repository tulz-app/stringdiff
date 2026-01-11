inThisBuild(
  List(
    organization := "app.tulz",
    homepage := Some(url("https://github.com/tulz-app/stringdiff")),
    licenses := List("MIT" -> url("https://github.com/tulz-app/stringdiff/blob/main/LICENSE.md")),
    scmInfo := Some(ScmInfo(url("https://github.com/tulz-app/stringdiff"), "scm:git@github.com/tulz-app/laminext.git")),
    developers := List(Developer("yurique", "Iurii Malchenko", "i@yurique.com", url("https://github.com/yurique"))),
    scalaVersion := ScalaVersions.v213,
    description := "String diff for Scala",
    crossScalaVersions := Seq(
      ScalaVersions.v212,
      ScalaVersions.v213,
      ScalaVersions.v3
    ),
    Test / publishArtifact := false,
    Test / parallelExecution := false,
    githubWorkflowJavaVersions += JavaSpec.temurin("17"),
    githubWorkflowTargetTags ++= Seq("v*"),
    githubWorkflowPublishTargetBranches := Seq(RefPredicate.StartsWith(Ref.Tag("v"))),
    githubWorkflowPublish := Seq(WorkflowStep.Sbt(List("ci-release"))),
    githubWorkflowEnv ~= (_ ++ Map(
      "PGP_PASSPHRASE"    -> s"$${{ secrets.PGP_PASSPHRASE }}",
      "PGP_SECRET"        -> s"$${{ secrets.PGP_SECRET }}",
      "SONATYPE_PASSWORD" -> s"$${{ secrets.SONATYPE_PASSWORD }}",
      "SONATYPE_USERNAME" -> s"$${{ secrets.SONATYPE_USERNAME }}"
    )),
    versionScheme := Some("early-semver")
  )
)

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
      ScalaOptions.fixOptions,
      libraryDependencies ++= Seq(
        "org.scalatest" %%% "scalatest" % "3.2.9" % Test
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
