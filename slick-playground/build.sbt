name := "slick-playground"

version := "1.0"

scalaVersion := "2.12.1"

libraryDependencies ++= List(
	"com.typesafe.slick" % "slick_2.12" % "3.2.0-M2",
	"com.h2database" % "h2" % "1.4.187",
	"mysql" % "mysql-connector-java" % "5.1.37",
	"org.slf4j" % "slf4j-simple" % "1.7.22",
	"org.scala-lang.modules" % "scala-java8-compat_2.12" % "0.8.0"
)
