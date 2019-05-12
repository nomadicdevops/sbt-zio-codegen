import sbt.plugins.SbtPlugin

enablePlugins(SbtPlugin)

name := "sbt-zio-codegen"

organization := "com.nomadicdevops"

version := "0.0.1"

scalaVersion := "2.12.8"

sbtPlugin := true

libraryDependencies ++= Seq(
  "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % "0.46.2" % Compile,
  "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % "0.46.2" % Provided,

  "org.scalaz" %% "scalaz-zio" % "1.0-RC4" % Test,
  "org.scalacheck" %% "scalacheck" % "1.14.0" % Test
)

