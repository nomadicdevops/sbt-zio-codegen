package com.nomadicdevops.sbt.zio.codegen.templates.enums

import com.nomadicdevops.sbt.zio.codegen.readers.{AppReader, EnumReader}
import com.nomadicdevops.sbt.zio.codegen.util.CodeGenUtil.getGenerator

object EnumGeneratorTemplate {

  def apply(
             appReader: AppReader,
             enumReader: EnumReader
           ): String = {
    val all: String = enumReader.subtypes.map(subtype =>
      subtype.fields match {
        case fields if fields.isEmpty => subtype.`type`
        case fields => s"""${subtype.`type`}(${
          fields.map { case (n, _) =>
            s"""
            |        $n = $n""".stripMargin
          }.mkString(", \t")
        }
            |       )""".stripMargin
      }
    ).mkString(",\n\t\t")

    val args: String = enumReader.subtypes.flatMap(subtype => subtype.fields.map {
      case (paramName, paramValue) => s"$paramName <- ${getGenerator(paramValue)}"
    }).mkString("\n\t")

    s"""package ${appReader.packages.generated}.enum.generators
       |
       |import org.scalacheck.Gen
       |import com.example.generated.enums._
       |
       |object ${enumReader.`type`}Gen {
       |  def apply(): Gen[${enumReader.`type`}] = for {
       |    $args
       |    enum <- Gen.oneOf(Seq(
       |      $all
       |    ))
       |  } yield enum
       |}
       """.stripMargin
  }

}
