name := "Akka-multiple-master-worker-instances"

scalaVersion := "2.9.2"

resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies ++= {
  val akkaVersion = "2.0.3"
  Seq(
    "ch.qos.logback" % "logback-classic" % "1.0.6" % "compile->default",
    "com.typesafe.akka" % "akka-actor" % akkaVersion,
    "com.typesafe.akka" % "akka-slf4j" % akkaVersion
  )
}
