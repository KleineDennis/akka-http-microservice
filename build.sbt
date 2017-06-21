name := """profiler-ws"""

version := "1.0"

scalaVersion := "2.11.8"

// Change this to another test framework if you prefer
libraryDependencies += "com.typesafe.akka" %% "akka-http" % "10.0.8"
libraryDependencies += "com.typesafe.akka" %% "akka-http-xml" % "10.0.8"
libraryDependencies += "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.8"
libraryDependencies += "com.typesafe.akka" %% "akka-http-testkit" % "10.0.8"
libraryDependencies += "com.typesafe.akka" % "akka-slf4j_2.11" % "2.4.19"

libraryDependencies += "org.apache.hadoop" % "hadoop-common" % "2.7.3"
libraryDependencies += "org.apache.hbase" % "hbase-common" % "1.1.2"
libraryDependencies += "org.apache.hbase" % "hbase-client" % "1.1.2"
libraryDependencies += "org.apache.hbase" % "hbase-server" % "1.1.2"



libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.3" % "test"

libraryDependencies += "com.github.danielwegener" % "logback-kafka-appender" % "0.1.0"
libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3"
