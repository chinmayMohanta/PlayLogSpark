name := "pop-test"

version := "0.1"

scalaVersion := "2.11.8"

resolvers ++= Seq(
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    "Confluent" at "http://packages.confluent.io/maven/"
)

libraryDependencies ++= Seq(
    "org.apache.spark" %% "spark-core" % "2.1.0" % "provided",
    "joda-time" % "joda-time" % "2.9.9",
    "org.apache.spark" %% "spark-sql" % "2.1.0"
)
