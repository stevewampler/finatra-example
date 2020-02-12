name := "example"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies ++= {
  val ficus = "1.4.0"
  val finatra = "2.9.0"
  val logback = "1.2.3"
  val play = "2.6.2"
  val sangria = "1.4.1"
  val sangriaPlayJson = "1.0.1"
  val fasterXML = "3.2.0"
  
  Seq(
    "com.iheart" %% "ficus" % ficus withSources(),
    "com.twitter" %% "finatra-http" % finatra withSources(),
    "ch.qos.logback" % "logback-classic" % logback withSources(),
    "org.sangria-graphql" %% "sangria" % sangria withSources(),
    "org.sangria-graphql" %% "sangria-play-json" % sangriaPlayJson withSources(),
    "com.typesafe.play" %% "play" % play,
    "com.typesafe.play" %% "play-json-joda" % play,
    "com.fasterxml.uuid" % "java-uuid-generator" % fasterXML withSources()
  )
}