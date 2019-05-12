package com.nomadicdevops.sbt.zio.codegen.writers

import com.nomadicdevops.sbt.zio.codegen.CodeGenConfig
import com.nomadicdevops.sbt.zio.codegen.readers.AppReader

object WriteGeneratedFile {

  def apply(
             appReader: AppReader,
             scalaClass: String,
             contents: String,
             subPackage: Option[String] = None
           )
           (implicit
            config: CodeGenConfig
           ): Unit = {
    val pathInitial = s"${config.srcMainScalaDir}/${appReader.packages.generated.replace(".", "/")}"
    val path = subPackage.map(subPackage => s"$pathInitial/$subPackage").getOrElse(pathInitial)
    WriteToDisk(path, scalaClass, contents)
  }

}
