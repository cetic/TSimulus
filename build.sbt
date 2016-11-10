// http://www.scala-sbt.org/0.13/docs/Multi-Project.html

organization := "be.cetic"
version := "0.1.3"
name := "rts-gen"
crossScalaVersions := Seq("2.11.8", "2.12.0")

// This forbids including Scala related libraries into the dependency
autoScalaLibrary := false

publishTo := {
   val nexus = "https://oss.sonatype.org/"
   if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
   else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

pomExtra := (
   <url>TODO</url>
      <licenses>
         <license>
            <name>BSD-style</name>
            <url>http://www.opensource.org/licenses/bsd-license.php</url>
            <distribution>repo</distribution>
         </license>
      </licenses>
      <scm>
         <url>git@github.com:jsuereth/scala-arm.git</url>
         <connection>scm:git:git@github.com:jsuereth/scala-arm.git</connection>
      </scm>
      <developers>
         <developer>
            <id>jsuereth</id>
            <name>Josh Suereth</name>
            <url>http://jsuereth.com</url>
         </developer>
      </developers>)

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

libraryDependencies ++= Seq(
   "com.github.nscala-time" %% "nscala-time" % "2.14.0",
   "org.apache.commons" % "commons-math3" % "3.6.1",
   "io.spray" %%  "spray-json" % "1.3.2"
)

libraryDependencies ++= Seq(
   "org.scala-lang" % "scala-reflect" % scalaVersion.value,
   "org.scala-lang.modules" %% "scala-xml" % "1.0.5"
)

libraryDependencies ++= Seq(
   "org.scalactic" %% "scalactic" % "3.0.0",
   "org.scalatest" %% "scalatest" % "3.0.0" % "test"
)