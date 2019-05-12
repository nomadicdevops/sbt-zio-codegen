package com.nomadicdevops.sbt.zio.codegen.templates.domain

import com.nomadicdevops.sbt.zio.codegen.readers.{AppReader, DomainReader}

object DomainTemplate {

  def apply(
             appReader: AppReader,
             domainReader: DomainReader
           ): String = {

    s"""
       |package ${appReader.packages.generated}.domain
       |
       |case class ${domainReader.`type`}(
       |${domainReader.fields.map(f => s"\t${f._1}: ${f._2}").mkString(", \n")}
       |)
       """.stripMargin

  }

}
