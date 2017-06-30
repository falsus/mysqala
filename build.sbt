organization := "com.git.falsus"

name := """mysqala"""

scalaVersion := "2.12.2"

version := "0.0.1-SNAPSHOT"

parallelExecution in Test := false

libraryDependencies ++= Seq(
  "org.javassist" % "javassist" % "3.14.0-GA",
  "mysql" % "mysql-connector-java" % "5.1.38",
  "com.h2database" % "h2" % "1.2.138",
  "junit" % "junit" % "4.8.2",
  "org.scalactic" %% "scalactic" % "3.0.3",
  "org.scalatest" %% "scalatest" % "3.0.3" % "test"
)


