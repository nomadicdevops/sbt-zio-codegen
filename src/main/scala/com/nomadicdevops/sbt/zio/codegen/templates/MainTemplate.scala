package com.nomadicdevops.sbt.zio.codegen.templates

import com.nomadicdevops.sbt.zio.codegen.readers.AppConfig

object MainTemplate {

  def apply(
             appConfig: AppConfig
           ): String = {

    s"""
       |package ${appConfig.packages.impl}
       |
       |import scalaz.zio.internal.PlatformLive
       |import scalaz.zio.{Runtime, ZIO}
       |import ${appConfig.packages.generated}.domain._
       |import ${appConfig.packages.generated}.services._
       |import ${appConfig.packages.generated}.enums._
       |import ${appConfig.packages.impl}.services._
       |
       |object Main extends App {
       |
       |  type ProgramEnv = ${appConfig.dependencies.mkString(" with ")}
       |  val programEnv: ProgramEnv = new ${appConfig.dependencies.map(d => s"Live$d").mkString(" with ")} ${if (appConfig.dependencies.size == 1) "{}" else ""}
       |
       |  val program: ZIO[ProgramEnv, ${appConfig.error}, Unit] = ???
       |  /*
       |    for {
       |      _ <- ??? // TODO: implement me
       |    } yield {
       |      ()
       |    }
       |  */
       |
       |  val runtime: Runtime[ProgramEnv] = Runtime(programEnv, PlatformLive.Default)
       |
       |  runtime
       |  .unsafeRun(program.either)
       |  .fold(_ => 1, _ => 0)
       |
       |}
       |
       """.stripMargin

  }

}
