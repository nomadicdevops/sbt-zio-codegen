package com.nomadicdevops.sbt.zio.codegen.templates.domain

import com.nomadicdevops.sbt.zio.codegen.readers.{AppReader, DomainReader}
import com.nomadicdevops.sbt.zio.codegen.util.CodeGenUtil.getGenerator

object DomainGeneratorTemplate {

  def apply(
             appReader: AppReader,
             domainReader: DomainReader
           ): String = {

    s"""
       |package ${appReader.packages.generated}.domain.generators
       |
         |import org.scalacheck.Gen
       |import ${appReader.packages.generated}.domain._
       |
         |object ${domainReader.`type`}Gen {
       |  def apply(): Gen[${domainReader.`type`}] = for {
    ${
      domainReader.fields.map {
        case (fieldName, fieldType) => s"$fieldName <- ${getGenerator(fieldType)}"
      }.mkString("\n\t\t")
    }
       |  } yield {
       |    ${domainReader.`type`}(
    ${
      domainReader.fields.map {
        case (fieldName, _) => s"\t$fieldName = $fieldName"
      }.mkString(", \n\t\t")
    }
       |    )
       |  }
       |}
       """.stripMargin
  }

}
