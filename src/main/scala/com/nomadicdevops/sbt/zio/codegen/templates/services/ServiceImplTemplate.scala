package com.nomadicdevops.sbt.zio.codegen.templates.services

import com.nomadicdevops.sbt.zio.codegen.readers.{AppConfig, ServiceReader}
import com.nomadicdevops.sbt.zio.codegen.util.CodeGenUtil.makeFirstLetterLowerCase

object ServiceImplTemplate {

  def apply(
             appConfig: AppConfig,
             serviceReader: ServiceReader
           ): String = {

    s"""package ${appConfig.packages.impl}.services
       |
       |import scalaz.zio.ZIO
       |import ${appConfig.packages.generated}.services._
       |import ${appConfig.packages.generated}.domain._
       |import ${appConfig.packages.generated}.enums._
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
             |\t): ZIO[Any, ${appConfig.error}, ${ioReader.output}] = ??? //TODO: implement me
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
