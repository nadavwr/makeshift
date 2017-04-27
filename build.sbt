lazy val commonSettings = Def.settings(
  scalaVersion := "2.11.8",
  organization := "com.github.nadavwr",
  version := "0.1.0",
  publishArtifact in (Compile, packageDoc) := false,
  licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
)

lazy val unpublished = Def.settings(
  publish := {},
  publishLocal := {},
  publishM2 := {}
)

lazy val makeshift = project
  .enablePlugins(ScalaNativePlugin)
  .settings(commonSettings)

lazy val sample = project
  .enablePlugins(ScalaNativePlugin)
  .settings(
    commonSettings,
    unpublished
  )
  .dependsOn(makeshift)

lazy val `makeshift-root` = (project in file("."))
  .aggregate(makeshift, sample)
  .settings(
    commonSettings,
    unpublished,
    run := { (run in sample).evaluated },
    publish := { (publish in makeshift).value }
  )

