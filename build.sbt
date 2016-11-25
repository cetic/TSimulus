// http://www.scala-sbt.org/0.13/docs/Multi-Project.html

organization := "be.cetic"
version := "0.1.6"
name := "rts-gen"

scalaVersion := "2.11.8"
crossScalaVersions := Seq("2.11.8", "2.12.0")

publishTo := {
   val nexus = "https://oss.sonatype.org/"
   if (isSnapshot.value)
      Some("snapshots" at nexus + "content/repositories/snapshots")
   else
      Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

pomExtra := (
   <url>https://github.com/cetic/rts-gen</url>
      <licenses>
         <license>
            <name>Apache License, Version 2.0</name>
            <url>https://www.apache.org/licenses/LICENSE-2.0.txt</url>
            <distribution>repo</distribution>
         </license>
      </licenses>
      <scm>
         <url>git@github.com:cetic/scala-arm.git</url>
         <connection>scm:git:git@github.com:cetic/rts-gen.git</connection>
      </scm>
      <developers>
         <developer>
            <id>mgoeminne</id>
            <name>Mathieu Goeminne</name>
            <email>mathieu.goeminne@cetic.be</email>
            <url>https://www.cetic.be/Mathieu-Goeminne</url>
            <organization>CETIC</organization>
            <organizationUrl>https://www.cetic.be</organizationUrl>
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