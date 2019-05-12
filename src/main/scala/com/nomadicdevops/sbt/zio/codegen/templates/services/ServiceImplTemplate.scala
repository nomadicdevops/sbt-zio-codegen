package com.nomadicdevops.sbt.zio.codegen.templates.services

import com.nomadicdevops.sbt.zio.codegen.readers.{AppReader, ServiceReader}
import com.nomadicdevops.sbt.zio.codegen.util.CodeGenUtil.makeFirstLetterLowerCase

object ServiceImplTemplate {

  def apply(
             appReader: AppReader,
             serviceReader: ServiceReader
           ): String = {

    s"""package ${appReader.packages.impl}.services
       |
       |import scalaz.zio.ZIO
       |import ${appReader.packages.generated}.services._
       |import ${appReader.packages.generated}.domain._
       |import ${appReader.packages.generated}.enums._
       |
       |class Live${serviceReader.`type`}Service extends ${serviceReader.`type`}.Service {
    ${
      serviceReader.interface.map {

        case (fName, ioReader) =>
          s"""
             |\toverride def $fName(
             |${
            ioReader.inputs.map {
              case (paramName, paramType) => s"""\t\t$paramName: $paramType"""
            }.mkString(",\n")
          }
             |\t): ZIO[Any, ${appReader.error}, ${ioReader.output}] = ???
           """.stripMargin

      }.mkString("\n")

    }
       |}
       |
       |trait Live${serviceReader.`type`} extends ${serviceReader.`type`} {
       |  val ${makeFirstLetterLowerCase(serviceReader.`type`)}Service: Live${serviceReader.`type`}Service = new Live${serviceReader.`type`}Service
       |}
       |
       |object Live${serviceReader.`type`} extends Live${serviceReader.`type`}
     """.stripMargin

  }

}
