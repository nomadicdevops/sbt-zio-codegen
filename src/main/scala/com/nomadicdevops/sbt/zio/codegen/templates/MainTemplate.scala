package com.nomadicdevops.sbt.zio.codegen.templates

import com.nomadicdevops.sbt.zio.codegen.readers.AppReader

object MainTemplate {

  def apply(
             appReader: AppReader
           ): String = {

    s"""
       |package ${appReader.packages.impl}
       |
       |import scalaz.zio.internal.PlatformLive
       |import scalaz.zio.{Runtime, ZIO}
       |import ${appReader.packages.generated}.domain._
       |import ${appReader.packages.generated}.services._
       |import ${appReader.packages.generated}.enums._
       |import ${appReader.packages.impl}.services._
       |
       |object Main extends App {
       |
       |  type ProgramEnv = ${appReader.dependencies.mkString(" with ")}
       |  val programEnv: ProgramEnv = new ${appReader.dependencies.map(d => s"Live$d").mkString(" with ")} ${if (appReader.dependencies.size == 1) "{}" else ""}
       |
       |  val program: ZIO[ProgramEnv, ${appReader.error}, Unit] = ???
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
