package com.nomadicdevops.sbt.zio.codegen.templates.services

import com.nomadicdevops.sbt.zio.codegen.readers.{AppReader, ServiceReader}
import com.nomadicdevops.sbt.zio.codegen.util.CodeGenUtil._

object ServiceTemplate {

  def apply(
             appReader: AppReader,
             serviceReader: ServiceReader
           ): String = {
      s"""package ${appReader.packages.generated}.services
         |
         |import scalaz.zio.ZIO
         |import ${appReader.packages.generated}.domain._
         |import ${appReader.packages.generated}.enums._
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
               |\t): ZIO[Any, ${appReader.error}, ${ioReader.output}]
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
               |\t): ZIO[${serviceReader.`type`}, ${appReader.error}, ${ioReader.output}] =
               |    ZIO.accessM(_.${makeFirstLetterLowerCase(serviceReader.`type`)}Service.$fName(${ioReader.inputs.keys.mkString(", ")}))
                 """.stripMargin

        }.mkString("\n")

      }
         |}
       """.stripMargin
  }

}
