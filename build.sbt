lazy val commonSettings = Def.settings(
  scalaVersion := "2.11.8",
  organization := "com.github.nadavwr",
  version := "0.1.1",
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

lazy val `makeshift-test` = project
  .enablePlugins(ScalaNativePlugin)
  .settings(
    commonSettings,
    unpublished
  )
  .dependsOn(makeshift)

lazy val `makeshift-root` = (project in file("."))
  .aggregate(makeshift, `makeshift-test`)
  .settings(
    commonSettings,
    unpublished,
    test := { (run in `makeshift-test`).toTask("").value },
    publish := { (publish in makeshift).value }
  )

