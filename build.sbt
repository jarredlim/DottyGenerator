val dottyVersion = "3.0.1-RC1"
val effpiVersion = "0.0.3"

val useEffpiPlugin = settingKey[Boolean]("Use the effpi compiler plugin in sub-projects.")

inThisBuild(
  // Can be changed from sbt by running `set ThisBuild / useEffpiPlugin := false`
  useEffpiPlugin := true
)

lazy val effpi = (project in file("./effpi")).
  settings(
    name := "effpi",
    version := effpiVersion,

    scalaVersion := dottyVersion,
    //addCompilerPlugin("uk.ac.ic" %% "effpi-verifier" % "0.0.3"),
  )

lazy val examples = project
  .dependsOn(effpi)
  .settings(
    name := "effpi-examples",
    version := effpiVersion,
    scalaVersion := dottyVersion,
  )

lazy val tests = project
  .in(file("effpi_sandbox"))
  .dependsOn(effpi)
  .settings(
    name := "effpi-sandbox",
    version := effpiVersion,
    scalaVersion := dottyVersion,
  )

lazy val case_studies = project
  .in(file("case_studies/sandbox"))
  .dependsOn(effpi)
  .settings(
    name := "case-study-sandbox",
    version := effpiVersion,
    scalaVersion := dottyVersion,
    libraryDependencies ++= Seq(
       "io.circe" %% "circe-core" % "0.14.0-M6",
       "io.circe" %% "circe-parser" % "0.14.0-M6",
    )
  )

lazy val pluginBenchmarks = project
  .in(file("./effpi/plugin-benchmarks"))
  .dependsOn(effpi)
  .settings(
    name := "effpi-plugin-benchmarks",
    version := effpiVersion,
    scalaVersion := dottyVersion,
  )
