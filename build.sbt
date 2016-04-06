name := "time-series-generator"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
   "com.github.nscala-time" % "nscala-time_2.11" % "2.12.0",
   "org.apache.commons" % "commons-math3" % "3.6.1",
   "io.spray" %%  "spray-json" % "1.3.2",
   "org.scalactic" %% "scalactic" % "2.2.6",
   "org.scalatest" %% "scalatest" % "2.2.6" % "test"
)