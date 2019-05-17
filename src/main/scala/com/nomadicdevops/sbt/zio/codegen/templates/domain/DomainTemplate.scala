package com.nomadicdevops.sbt.zio.codegen.templates.domain

import com.nomadicdevops.sbt.zio.codegen.readers.{AppConfig, DomainReader}

object DomainTemplate {

  def apply(
             appConfig: AppConfig,
             domainReader: DomainReader
           ): String = {

    s"""
       |package ${appConfig.packages.generated}.domain
       |
       |import ${appConfig.packages.generated}.enums._
       |
       |case class ${domainReader.`type`}(
       |${domainReader.fields.map(f => s"\t${f._1}: ${f._2}").mkString(", \n")}
       |)
       """.stripMargin

  }

}
