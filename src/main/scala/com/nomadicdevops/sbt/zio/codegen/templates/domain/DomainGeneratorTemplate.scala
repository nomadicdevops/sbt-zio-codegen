package com.nomadicdevops.sbt.zio.codegen.templates.domain

import com.nomadicdevops.sbt.zio.codegen.readers.{AppConfig, DomainReader}
import com.nomadicdevops.sbt.zio.codegen.util.CodeGenUtil.getGenerator

object DomainGeneratorTemplate {

  def apply(
             appConfig: AppConfig,
             domainReader: DomainReader
           ): String = {

    s"""
       |package ${appConfig.packages.generated}.domain.generators
       |
       |import org.scalacheck.Gen
       |import ${appConfig.packages.generated}.domain._
       |import ${appConfig.packages.generated}.enum.generators._
       |import ${appConfig.packages.generated}.util._
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
