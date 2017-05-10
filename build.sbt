import sbtcrossproject.{crossProject, CrossType}

val Scala_2_12 = "2.12.2"
val Scala_2_11 = "2.11.11"
def crossAlias(aliasName: String, commandName: String, projectNames: String*): Command =
  BasicCommands.newAlias(aliasName, {
    projectNames
      .map { projectName =>
        s""";++$Scala_2_12
           |;${projectName}JVM/$commandName
           |;++$Scala_2_11
           |;${projectName}JVM/$commandName
           |;${projectName}Native/$commandName
         """.stripMargin
      }.mkString
    })

def forallAlias(aliasName: String, commandName: String, projectNames: String*): Command =
  BasicCommands.newAlias(aliasName, {
    projectNames
      .map { projectName =>
        s""";${projectName}JVM/$commandName
           |;${projectName}Native/$commandName
         """.stripMargin
      }.mkString
    })

lazy val commonSettings = Def.settings(
  scalaVersion := Scala_2_11,
  organization := "com.github.nadavwr",
  publishArtifact in (Compile, packageDoc) := false,
  licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
)

lazy val unpublished = Def.settings(
  publish := {},
  publishLocal := {},
  publishM2 := {}
)

lazy val makeshift = crossProject(JVMPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .settings(
    commonSettings,
    moduleName := "makeshift"
  )

lazy val makeshiftJVM = makeshift.jvm
lazy val makeshiftNative = makeshift.native

lazy val makeshiftTest = crossProject(JVMPlatform, NativePlatform)
  .crossType(CrossType.Full)
  .settings(
    commonSettings,
    unpublished,
    test := { (run in Compile).toTask("").value }
  )
  .nativeSettings(
    run in Compile := { run.toTask("").value }
  )
  .dependsOn(makeshift)

lazy val makeshiftTestJVM = makeshiftTest.jvm
lazy val makeshiftTestNative = makeshiftTest.native

lazy val makeshiftRoot = (project in file("."))
  .settings(
    commonSettings,
    unpublished,
    commands += crossAlias("publishLocal", "publishLocal", "makeshift"),
    commands += crossAlias("publish", "publish", "makeshift"),
    commands += crossAlias("test", "test", "makeshiftTest"),
    commands += forallAlias("clean", "clean", "makeshift", "makeshiftTest")
  )
