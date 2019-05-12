package com.nomadicdevops.sbt.zio.codegen.templates.domain

import com.nomadicdevops.sbt.zio.codegen.readers.{AppReader, DomainReader}
import com.nomadicdevops.sbt.zio.codegen.util.CodeGenUtil.{getGenericTypes, getGenericTypesAsList, stripGenericTypes}

object GenericDomainGeneratorTemplate {

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
       |object ${stripGenericTypes(domainReader.`type`)}Gen {
       |  def apply${getGenericTypes(domainReader.`type`)}(
       |  ${getGenericTypesAsList(domainReader.`type`).map(t => s"${t}Generator: Gen[$t]").mkString(",\n")}
       |  ): Gen[${domainReader.`type`}] = for {
    ${
      domainReader.fields.map {
        case (fieldName, fieldType) => s"$fieldName <- ${fieldType}Generator"
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
