package com.nomadicdevops.sbt.zio.codegen.templates.enums

import com.nomadicdevops.sbt.zio.codegen.readers.{AppReader, EnumReader}

object EnumTemplate {

  def apply(
             appReader: AppReader,
             enumReader: EnumReader
           ): String = {
    s"""package ${appReader.packages.generated}.enums
       |
       |sealed trait ${enumReader.`type`}
        ${
      enumReader.subtypes.map(subtype =>
        subtype.fields match {
          case fields if fields.isEmpty => s"""case object ${subtype.`type`} extends ${enumReader.`type`}"""
          case fields => s"""
                            |case class ${subtype.`type`}(${
            fields.map { case (n, v) => s"\n\t$n: $v" }.mkString(",")
          }) extends ${enumReader.`type`}""".stripMargin
        }
      ).mkString("\n\n")
    }
       """.stripMargin
  }

}
