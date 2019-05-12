package com.nomadicdevops.sbt.zio

import sbt._
import Keys._
import com.nomadicdevops.sbt.zio.codegen.{CodeGen, CodeGenConfig}

object ZioCodeGenPlugin extends AutoPlugin {

  override val requires: Plugins = plugins.JvmPlugin

  override def globalSettings: Seq[Setting[_]] = Nil

  override def trigger = allRequirements

  object autoImport extends ZioCodeGenKeys

  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]] = Seq(
    // defaults
    zioCodeGenApiDir := (Compile / resourceDirectory).value, // project's src/main/resources
    zioCodeGenSrcMainScalaDir := (Compile / scalaSource).value, // project's /src/main/scala
    zioCodeGenSrcTestScalaDir := (Test / scalaSource).value, // project's /src/test/scala

    zioCodeGen := zioCodeGenTask.value,
    zioCodeGenImpl := zioCodeGenImplTask.value,
    zioCodeGenAll := zioCodeGenAllTask.value
  )

  private def runWithCodeGenConfig(f: CodeGenConfig => Unit): Unit =  Def.task {
    implicit lazy val config: CodeGenConfig = CodeGenConfig(
      apiDir = zioCodeGenApiDir.value.getAbsolutePath,
      srcMainScalaDir = zioCodeGenSrcMainScalaDir.value.getAbsolutePath,
      srcTestScalaDir = zioCodeGenSrcTestScalaDir.value.getAbsolutePath
    )

    val log = sLog.value
    log.info(s"Generating ZIO files...")
    f(config)
    log.info(s"Done. Happy Coding!")
  }


  private def zioCodeGenAllTask: Def.Initialize[Task[Unit]] = Def.task {
    runWithCodeGenConfig { config =>
      CodeGen.createGeneratedFiles(config)
      CodeGen.createImplFiles(config)
    }
  }

  private def zioCodeGenImplTask: Def.Initialize[Task[Unit]] = Def.task {
    runWithCodeGenConfig { config =>
      CodeGen.createImplFiles(config)
    }
  }

  private def zioCodeGenTask: Def.Initialize[Task[Unit]] = Def.task {
    runWithCodeGenConfig { config =>
      CodeGen.createGeneratedFiles(config)
    }
  }

}