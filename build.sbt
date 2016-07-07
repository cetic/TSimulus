// http://www.scala-sbt.org/0.13/docs/Multi-Project.html

lazy val commonSettings = Seq(
   organization := "be.cetic",
   version := "0.1.0",
   scalaVersion := "2.11.8"
)


lazy val root = (project in file(".")).
   aggregate(service, core)

lazy val core = (project in file("ts-core")).
   settings(commonSettings: _*).
   settings(
      name := "ts-generator",
      libraryDependencies ++= commonLibDependencies,
      libraryDependencies ++= Seq(
         "com.github.nscala-time" % "nscala-time_2.11" % "2.12.0",
         "org.apache.commons" % "commons-math3" % "3.6.1",
         "io.spray" %%  "spray-json" % "1.3.2",
         "org.scalactic" %% "scalactic" % "2.2.6",
         "org.scalatest" %% "scalatest" % "2.2.6" % "test"
      )
   )

lazy val service = (project in file("ts-service")).
   settings(commonSettings: _*).
   settings(
      name := "ts-service"
   ).
   dependsOn(core)

lazy val commonLibDependencies = Seq(
   "org.scala-lang" % "scala-reflect" % "2.11.7",
   "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.4"
)