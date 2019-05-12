package com.nomadicdevops.sbt.zio.codegen.writers

import com.nomadicdevops.sbt.zio.codegen.CodeGenConfig
import com.nomadicdevops.sbt.zio.codegen.readers.AppReader

object WriteTestFile {

  def apply(
             appReader: AppReader,
             scalaClass: String,
             contents: String,
             subPackage: Option[String] = None
           )
           (implicit
            config: CodeGenConfig
           ): Unit = {
    val pathInitial = s"${config.srcTestScalaDir}/${appReader.packages.generated.replace(".", "/")}"
    val path = subPackage.map(sp => s"$pathInitial/$sp").getOrElse(pathInitial)
    WriteToDisk(path, scalaClass, contents)
  }

}
