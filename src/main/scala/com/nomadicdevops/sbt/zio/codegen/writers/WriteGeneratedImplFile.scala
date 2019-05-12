package com.nomadicdevops.sbt.zio.codegen.writers

import com.nomadicdevops.sbt.zio.codegen.CodeGenConfig
import com.nomadicdevops.sbt.zio.codegen.readers.AppReader

object WriteGeneratedImplFile {

  def apply(
             appReader: AppReader,
             scalaClass: String,
             contents: String,
             subPackage: Option[String] = None
           )
           (implicit
            config: CodeGenConfig
           ): Unit = {
    val pathInitial = s"${config.srcMainScalaDir}/${appReader.packages.impl.replace(".", "/")}"
    val path = subPackage.map(sp => s"$pathInitial/$sp").getOrElse(pathInitial)
    WriteToDisk(path, scalaClass, contents)
  }

}
