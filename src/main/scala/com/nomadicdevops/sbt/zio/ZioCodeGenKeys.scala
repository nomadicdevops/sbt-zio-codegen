package com.nomadicdevops.sbt.zio

import sbt._
import java.io.File

trait ZioCodeGenKeys {
  // in
  lazy val zioCodeGenApiDir = settingKey[File]("Where API in JSON is defined. By default in resources. Example: (Compile / resourceDirectory).value")

  // in - app config
  lazy val zioCodeGenGeneratedPackageName = settingKey[String]("Meant for files that do not require future modification and can be regenerated in the future")
  lazy val zioCodeGenGeneratedImplPackageName = settingKey[String]("Meant for files that require modification and should not be regenerated/overwritten")
  lazy val zioCodeGenErrorType = settingKey[String]("Error type. This is the E in ZIO[R, E, A] Example: Throwable")

  // in - enable/disable generation
  lazy val zioCodeGenForImpl = settingKey[Boolean]("Generate impl sources or not")

  // out
  lazy val zioCodeGenSrcMainScalaDir = settingKey[File]("project's src/main/scala dir. Example: (Compile / scalaSource).value")
  lazy val zioCodeGenSrcTestScalaDir = settingKey[File]("project's src/test/scala dir. Example: (Test / scalaSource).value")

  // task
  lazy val zioCodeGen = taskKey[Unit]("Generates zio files")
  lazy val zioCodeGenClean = taskKey[Unit]("Deletes ALL generated zio files")

}