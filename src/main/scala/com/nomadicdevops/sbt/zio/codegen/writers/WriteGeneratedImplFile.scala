package com.nomadicdevops.sbt.zio.codegen.writers

import com.nomadicdevops.sbt.zio.codegen.CodeGenConfig
import com.nomadicdevops.sbt.zio.codegen.readers.AppConfig

object WriteGeneratedImplFile {

  def apply(
             appConfig: AppConfig,
             scalaClass: String,
             contents: String,
             subPackage: Option[String] = None
           )
           (implicit
            config: CodeGenConfig
           ): Unit = {
    val pathInitial = s"${config.srcMainScalaDir}/${appConfig.packages.impl.replace(".", "/")}"
    val path = subPackage.map(sp => s"$pathInitial/$sp").getOrElse(pathInitial)
    WriteToDisk(path, scalaClass, contents)
  }

}
