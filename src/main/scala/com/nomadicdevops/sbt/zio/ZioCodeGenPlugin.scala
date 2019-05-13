package com.nomadicdevops.sbt.zio

import com.github.plokhotnyuk.jsoniter_scala.core.{JsonValueCodec, writeToArray}
import com.github.plokhotnyuk.jsoniter_scala.macros.{CodecMakerConfig, JsonCodecMaker}
import com.nomadicdevops.sbt.zio.codegen.{CodeGen, CodeGenConfig}
import sbt.Keys._
import sbt._

object ZioCodeGenPlugin extends AutoPlugin {

  override val requires: Plugins = plugins.JvmPlugin

  override def globalSettings: Seq[Setting[_]] = Nil

  override def trigger: PluginTrigger = allRequirements

  object autoImport extends ZioCodeGenKeys

  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    // defaults
    zioCodeGenApiDir := (Compile / resourceDirectory).value, // project's src/main/resources
    zioCodeGenSrcMainScalaDir := (Compile / scalaSource).value, // project's /src/main/scala
    zioCodeGenSrcTestScalaDir := (Test / scalaSource).value, // project's /src/test/scala

    // app config
    zioCodeGenGeneratedPackageName := "com.example.zio.generated",
    zioCodeGenGeneratedImplPackageName := "com.example.zio.impl",
    zioCodeGenErrorType := "Throwable",

    // enable/disable generation
    zioCodeGenForImpl := true, // generate impl sources

    // not sure if needed :)
    zioCodeGen := zioCodeGenTask.value,
    zioCodeGenClean := zioCodeGenCleanTask.value
  )

  implicit val codeGenConfigCodec: JsonValueCodec[CodeGenConfig] = JsonCodecMaker.make[CodeGenConfig](CodecMakerConfig())

  private def zioCodeGenTask: Def.Initialize[Task[Unit]] = Def.task {
    val log = sLog.value

    implicit val config: CodeGenConfig = CodeGenConfig(
      apiDir = zioCodeGenApiDir.value.getAbsolutePath,
      srcMainScalaDir = zioCodeGenSrcMainScalaDir.value.getAbsolutePath,
      srcTestScalaDir = zioCodeGenSrcTestScalaDir.value.getAbsolutePath,
      zioCodeGenGeneratedPackageName = zioCodeGenGeneratedPackageName.value,
      zioCodeGenGeneratedImplPackageName = zioCodeGenGeneratedImplPackageName.value,
      zioCodeGenErrorType = zioCodeGenErrorType.value,
      servicesDir = zioCodeGenApiDir.value.getAbsolutePath
    )

    log.info(s"Generating ZIO files... with config:")
    log.info(new String(writeToArray(config)))
    CodeGen.createGeneratedFiles(config)
    if (zioCodeGenForImpl.value)
      CodeGen.createImplFiles(config)
    log.info(s"Done. Happy Coding!")

  }

  private def zioCodeGenCleanTask: Def.Initialize[Task[Unit]] = Def.task {
    val log = sLog.value

    implicit val config: CodeGenConfig = CodeGenConfig(
      apiDir = zioCodeGenApiDir.value.getAbsolutePath,
      srcMainScalaDir = zioCodeGenSrcMainScalaDir.value.getAbsolutePath,
      srcTestScalaDir = zioCodeGenSrcTestScalaDir.value.getAbsolutePath,
      zioCodeGenGeneratedPackageName = zioCodeGenGeneratedPackageName.value,
      zioCodeGenGeneratedImplPackageName = zioCodeGenGeneratedImplPackageName.value,
      zioCodeGenErrorType = zioCodeGenErrorType.value,
      servicesDir = zioCodeGenApiDir.value.getAbsolutePath
    )

    log.info(s"Deleting generated ZIO files...")
    import scala.reflect.io.Directory
    List(
      new Directory(new File(s"${config.srcMainScalaDir}/${config.appConfig.packages.generated.replace(".", "/")}")),
      new Directory(new File(s"${config.srcMainScalaDir}/${config.appConfig.packages.impl.replace(".", "/")}")),
      new Directory(new File(s"${config.srcTestScalaDir}/${config.appConfig.packages.generated.replace(".", "/")}"))
    ).foreach(_.deleteRecursively())

    log.info(s"Done. All Cleaned Up!")

  }

}