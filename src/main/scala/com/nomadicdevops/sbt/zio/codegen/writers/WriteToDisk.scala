package com.nomadicdevops.sbt.zio.codegen.writers

import java.io.{BufferedWriter, File, FileWriter}

object WriteToDisk {

  def apply(
             path: String,
             scalaClass: String,
             contents: String
           ): Unit = {
    new File(path).mkdirs()
    val file = new File(s"$path/$scalaClass.scala")
    val bw = new BufferedWriter(new FileWriter(file, false))
    bw.write(contents)
    bw.close()
  }

}
