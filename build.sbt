name := "stream-example"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.7"

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8", // yes, this is 2 args
  "-feature",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  // "-Ywarn-dead-code", // N.B. doesn't work well with the ??? hole
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture")

resolvers ++= Seq(
  "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
)

resolvers += "Sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies ++= {
  val scalazVer = "7.1.0"    // or "latest.release"
  Seq(
    // see https://github.com/etorreborre/specs2/issues/296#issuecomment-94796264
    "org.scalatest" %% "scalatest" % "latest.release" % "test",
    "org.scalacheck" %% "scalacheck" % "latest.release" % "test",
    "com.typesafe.akka" %% "akka-stream-experimental" % "1.0",
    "org.scalaz" %% "scalaz-core" % scalazVer,
    "org.scalaz" %% "scalaz-effect" % scalazVer
  )
}
