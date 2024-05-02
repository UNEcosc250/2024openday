
// shadow sbt-scalajs' crossProject and CrossType from Scala.js 0.6.x
import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

name := "assessory"
scalaVersion := "3.3.3"
organization := "com.wbillingsley"
version := "0.4.0-SNAPSHOT"

def useScala3 = (scalaVersion := "3.3.3")
def useScala2 = (scalaVersion := "2.13.7")

lazy val commonSettings = Seq(
  organization := "com.wbillingsley",
  version := "0.1-SNAPSHOT",
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
  resolvers ++= Seq(
    "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/",
    "sonatype releases" at "https://oss.sonatype.org/content/repositories/releases/",
    "jitpack" at "https://jitpack.io",
    DefaultMavenRepository
  ),
  libraryDependencies ++= Seq(
    //Handy
   "org.scalactic" %% "scalactic" % "3.2.9",
    "org.scalatest" %% "scalatest" % "3.2.9" % "test",
    "org.scalameta" %% "munit" % "0.7.29" % Test

  )

)

val circeVersion = "0.14.1"
lazy val api = (crossProject(JSPlatform, JVMPlatform).crossType(CrossType.Pure) in file("modules/api"))
  .settings(commonSettings:_*)
  .settings(
    useScala3,
    libraryDependencies ++= Seq(
      "com.github.wbillingsley.handy" %%% "handy" % "v0.11-SNAPSHOT",
    ),
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "upickle" % "3.2.0",
      // "io.circe" %%% "circe-core",
      // "io.circe" %%% "circe-parser"
    ) // .map(_ % circeVersion)

  )

lazy val apiJS = api.js
lazy val apiJVM = api.jvm

lazy val vclient = project.in(file("modules/vclient"))
  .settings(commonSettings:_*)
  .settings(
    useScala3,
    scalaJSUseMainModuleInitializer := true,
    Test / scalaJSUseMainModuleInitializer := false,
    libraryDependencies ++= Seq(
      "com.wbillingsley" %%% "doctacular" % "0.3.0",
    )
  )
  .dependsOn(apiJS)
  .enablePlugins(ScalaJSPlugin, JSDependenciesPlugin, ScalaJSWeb)

lazy val sjsProjects = Seq(vclient)



// The web layer
val PekkoVersion = "1.0.2"
val PekkoHttpVersion = "1.0.1"

val prod = true

lazy val pekkohttp = (project in file("modules/pekkoHttp"))
  .dependsOn(apiJVM)
  .settings(commonSettings:_*)
  .aggregate(sjsProjects.map(sbt.Project.projectToRef):_*)
  .settings(
    useScala3,

    libraryDependencies ++= Seq(
      // JavaScript
      "org.webjars" % "bootstrap" % "4.4.1-1",
      "org.webjars" % "font-awesome" % "4.5.0",
      "org.webjars" % "marked" % "0.3.2-1"
    ),

    scalaJSProjects := sjsProjects,
    Assets / pipelineStages := Seq(scalaJSPipeline),
    pipelineStages := Seq(scalaJSPipeline),
    // triggers scalaJSPipeline when using compile or continuous compilation
    Compile / compile := ((Compile / compile) dependsOn scalaJSPipeline).value,
    libraryDependencies ++= Seq(
      ("org.apache.pekko" %% "pekko-actor-typed" % PekkoVersion),
      ("org.apache.pekko" %% "pekko-stream" % PekkoVersion),
      ("org.apache.pekko" %% "pekko-http" % PekkoHttpVersion),

      "org.apache.logging.log4j" % "log4j-slf4j-impl" % "2.17.1"
    ),
    Assets / WebKeys.packagePrefix := "public/",
    Runtime / managedClasspath += (Assets / packageBin).value,

    if (prod) {
      (Compile / resources) += (vclient / Compile / fullOptJS).value.data
    } else {
      (Compile / resources) += (vclient / Compile / fastOptJS).value.data
    }

  ).enablePlugins(SbtWeb, JavaAppPackaging)


// We also need to register munit as a test framework in sbt so that "sbt test" will work and the IDE will recognise
// tests
testFrameworks += new TestFramework("munit.Framework")
