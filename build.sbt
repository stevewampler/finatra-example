name := "example"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies ++= {
  val finatra = "2.9.0"
  val logback = "1.2.3"

  Seq(
    "com.twitter" %% "finatra-http" % finatra,
    "ch.qos.logback" % "logback-classic" % logback
  )
}