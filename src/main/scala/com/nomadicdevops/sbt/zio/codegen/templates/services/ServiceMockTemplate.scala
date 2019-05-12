package com.nomadicdevops.sbt.zio.codegen.templates.services

import com.nomadicdevops.sbt.zio.codegen.readers.{AppReader, ServiceReader}
import com.nomadicdevops.sbt.zio.codegen.util.CodeGenUtil.{makeFirstLetterLowerCase, stripGenericTypes}

object ServiceMockTemplate {

  def apply(
             appReader: AppReader,
             serviceReader: ServiceReader
           ): String = {

    s"""package ${appReader.packages.generated}.service.mocks
       |
       |import scalaz.zio.ZIO
       |import ${appReader.packages.generated}.services._
       |import ${appReader.packages.generated}.domain._
       |import ${appReader.packages.generated}.enums._
       |
       |object Mock${serviceReader.`type`} {
       |  def apply(
       |    ${serviceReader.interface.map { case (fName, ioReader) => s"${stripGenericTypes(fName)}Returns: Option[Either[${appReader.error}, ${ioReader.output}]] = None" }.mkString(",\n\t")}
       |           ): ${serviceReader.`type`} = new ${serviceReader.`type`} {
       |    override def ${makeFirstLetterLowerCase(serviceReader.`type`)}Service: ${serviceReader.`type`}.Service = new ${serviceReader.`type`}.Service {
    ${
      serviceReader.interface.map {

        case (fName, ioReader) =>
          s"""
\toverride def $fName(
${
            ioReader.inputs.map {
              case (paramName, paramType) => s"""\t\t$paramName: $paramType"""
            }.mkString(",\n")
          }
\t): ZIO[Any, ${appReader.error}, ${ioReader.output}] = ${stripGenericTypes(fName)}Returns match {
             |        case Some(Right(good)) => ZIO.succeed(good)
             |        case Some(Left(bad)) => ZIO.fail(bad)
             |        case _ => throw new NotImplementedError("Mock${serviceReader.`type`}.${fName} was not defined. Check if ${fName}Returns is set on Mock${serviceReader.`type`}")
             |      }
           """.stripMargin

      }.mkString("\n")

    }
       |    }
       |  }
       |}
       """.stripMargin

  }


}
