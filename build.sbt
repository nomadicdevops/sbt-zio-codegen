import sbt.plugins.SbtPlugin

enablePlugins(SbtPlugin)

name := "sbt-zio-codegen"

organization := "com.nomadicdevops"

version := "0.0.3-SNAPSHOT"

scalaVersion := "2.12.8"

sbtPlugin := true

libraryDependencies ++= Seq(
  "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-core" % "0.46.2" % Compile,
  "com.github.plokhotnyuk.jsoniter-scala" %% "jsoniter-scala-macros" % "0.46.2" % Provided,

  "org.scalaz" %% "scalaz-zio" % "1.0-RC4" % Test,
  "org.scalacheck" %% "scalacheck" % "1.14.0" % Test
)


useGpg := true
pgpReadOnly := false
pgpSecretRing := pgpPublicRing.value

// POM settings for Sonatype
homepage := Some(url("https://github.com/nomadicdevops/sbt-zio-codegen"))
scmInfo := Some(ScmInfo(url("https://github.com/nomadicdevops/sbt-zio-codegen"),
"https://github.com/nomadicdevops/sbt-zio-codegen.git"))
developers := List(Developer("NomadicDevOps",
  "Nomadic DevOps",
  "info@nomadicdevops.com",
  url("https://github.com/nomadicdevops")))
licenses += ("MIT", url("https://opensource.org/licenses/MIT"))
publishMavenStyle := true

// Add sonatype repository settings
publishTo := Some(
  if (isSnapshot.value)
    Opts.resolver.sonatypeSnapshots
  else
    Opts.resolver.sonatypeStaging
)

