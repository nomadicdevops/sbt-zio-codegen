package com.nomadicdevops.sbt.zio.codegen.templates.services

import com.nomadicdevops.sbt.zio.codegen.readers.{AppConfig, ServiceReader}
import com.nomadicdevops.sbt.zio.codegen.util.CodeGenUtil._

object ServiceTemplate {

  def apply(
             appConfig: AppConfig,
             serviceReader: ServiceReader
           ): String = {
      s"""package ${appConfig.packages.generated}.services
         |
         |import scalaz.zio.ZIO
         |import ${appConfig.packages.generated}.domain._
         |import ${appConfig.packages.generated}.enums._
         |
         |object ${serviceReader.`type`} {
         |  trait Service {
         |    ${
        serviceReader.interface.map {

          case (fName, ioReader) =>
            s"""
               |\tdef $fName(
               |${
              ioReader.inputs.map {
                case (paramName, paramType) => s"""\t\t$paramName: $paramType"""
              }.mkString(",\n")
            }
               |\t): ZIO[Any, ${appConfig.error}, ${ioReader.output}]
           """.stripMargin

        }.mkString("\n")

      }
         |  }
         |}
         |
         |trait ${serviceReader.`type`} {
         |  def ${makeFirstLetterLowerCase(serviceReader.`type`)}Service: ${serviceReader.`type`}.Service
         |}
         |
         |object ${serviceReader.`type`}Provider {
        ${
        serviceReader
          .interface.map {


          case (fName, ioReader) =>

            s"""
               |\tdef $fName(
               |${
              ioReader
                .inputs.map {
                case (paramName, paramType) =>
                  s"""\t\t$paramName: $paramType"""
              }.mkString(",\n")

            }
               |\t): ZIO[${serviceReader.`type`}, ${appConfig.error}, ${ioReader.output}] =
               |    ZIO.accessM(_.${makeFirstLetterLowerCase(serviceReader.`type`)}Service.$fName(${ioReader.inputs.keys.mkString(", ")}))
                 """.stripMargin

        }.mkString("\n")

      }
         |}
       """.stripMargin
  }

}
