name := "Scala-Ghost-Lib"

version := "0.0.1"

scalaVersion := "2.10.3"

resolvers ++= Seq(
  "Typesafe Repository"  at "http://repo.typesafe.com/typesafe/releases/",
  "spray" at "http://repo.spray.io",
  "Big Bee Consultants" at "http://repo.bigbeeconsultants.co.uk/repo"
)

libraryDependencies ++= Seq(
  "uk.co.bigbeeconsultants" %% "bee-client" % "0.21.+",
  "org.slf4j" % "slf4j-simple" % "1.7.5",
  "org.json4s" %% "json4s-native" % "3.2.6"
)

