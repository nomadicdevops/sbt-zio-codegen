package com.nomadicdevops.sbt.zio.codegen.templates.services

import com.nomadicdevops.sbt.zio.codegen.readers.{AppConfig, ServiceInterface, ServiceReader}
import com.nomadicdevops.sbt.zio.codegen.util.CodeGenUtil.{accountForFunctionType, stripGenericTypes}

object ServiceMockSpecTemplate {

  def apply(
             appConfig: AppConfig,
             serviceReader: ServiceReader
           ): String = {
    val kvs1 = serviceReader.interface.foldLeft(List[(String, String)]()) {
      case (acc, (functionName, ServiceInterface(inputs, output))) =>
        acc :+ (s"${output}Gen()", s"$functionName: $output")
    }

    val kvs2 = serviceReader.interface.foldLeft(List[(String, String)]()) {
      case (acc, (functionName, ServiceInterface(inputs, output))) => {
        acc ::: inputs.map {
          case (functionArgName, functionArgType) => (s"${functionArgType}Gen()", s"${functionName}_$functionArgName: $functionArgType")
        }.toList

      }
    }

    val generators = (kvs1 ::: kvs2).map(a => accountForFunctionType(stripGenericTypes(a._1))).mkString(",\n")
    val generatorsMatchingArgs = (kvs1 ::: kvs2).map(a => stripGenericTypes(a._2)).mkString(",\n")

    val failureGenerators = (s"${appConfig.error}Gen()" +: kvs2.map(a => stripGenericTypes(a._1))).mkString(",\n")
    val failureGeneratorsMatchingArgs = (s"err: ${appConfig.error}" +: kvs2.map(a => stripGenericTypes(a._2))).mkString(",\n")

    s"""
       |package ${appConfig.packages.generated}.service.mock.specs
       |
         |import ${appConfig.packages.generated}.domain._
       |import ${appConfig.packages.generated}.domain.generators._
       |import ${appConfig.packages.generated}.enum.generators._
       |import ${appConfig.packages.generated}.util._
       |import ${appConfig.packages.generated}.enums._
       |import ${appConfig.packages.generated}.service.mocks._
       |import ${appConfig.packages.generated}.services._
       |import org.scalacheck.Prop.forAll
       |import org.scalacheck.Properties
       |import scalaz.zio.internal.PlatformLive
       |import scalaz.zio.{Runtime, ZIO}
       |
 |
 |object Mock${serviceReader.`type`}Spec extends Properties("Mock${serviceReader.`type`}Spec") {
       |
 |  property("mock ${serviceReader.`type`} success spec") = forAll(
       |  ${generators}
       |  ) {
       |    (
       |      ${generatorsMatchingArgs}
       |    ) =>
       |
         |      type ProgramEnv = ${serviceReader.`type`}
       |      val programEnv: ProgramEnv = Mock${serviceReader.`type`}(
       |        ${

      serviceReader.interface.map {
        case (fieldName, ServiceInterface(_, outputType)) => s"${stripGenericTypes(fieldName)}Returns = Option(Right(${stripGenericTypes(fieldName)}))"
      }.mkString(",\n\t")
    }
       |      )
       |      val runtime: Runtime[ProgramEnv] = Runtime(programEnv, PlatformLive.Default)
       |
 |       ${
      serviceReader.interface.map {
        case (functionName, ServiceInterface(inputs, outputType)) =>
          s"""
             |      val ${functionName}Program: ZIO[ProgramEnv, ${appConfig.error}, $outputType] =
             |        for {
             |          res <- ${serviceReader.`type`}Provider.${functionName}(
             |          ${inputs.map { case (fieldName, _) => s"${functionName}_$fieldName" }.mkString(",\n")}
             |          )
             |        } yield {
             |          res
             |        }""".stripMargin
      }.mkString("\n\t")
    }
         ${
      serviceReader.interface.map {
        case (functionName, ServiceInterface(inputs, outputType)) =>
          s"      runtime.unsafeRun(${functionName}Program.either) == Right($functionName)"
      }.mkString("\n\t")

    }
       |  }


       |  property("mock ${serviceReader.`type`} failure spec") = forAll(
       |  ${failureGenerators}
       |  ) {
       |    (
       |      ${failureGeneratorsMatchingArgs}
       |    ) =>
       |
         |      type ProgramEnv = ${serviceReader.`type`}
       |      val programEnv: ProgramEnv = Mock${stripGenericTypes(serviceReader.`type`)}(
       |        ${

      serviceReader.interface.map {
        case (fieldName, ServiceInterface(_, outputType)) => s"${stripGenericTypes(fieldName)}Returns = Option(Left(err))"
      }.mkString(",\n\t")
    }
       |      )
       |      val runtime: Runtime[ProgramEnv] = Runtime(programEnv, PlatformLive.Default)
       |
 |       ${
      serviceReader.interface.map {
        case (functionName, ServiceInterface(inputs, outputType)) =>
          s"""
             |      val ${functionName}Program: ZIO[ProgramEnv, ${appConfig.error}, $outputType] =
             |        for {
             |          res <- ${serviceReader.`type`}Provider.${functionName}(
             |          ${inputs.map { case (fieldName, _) => s"${functionName}_$fieldName" }.mkString(",\n")}
             |          )
             |        } yield {
             |          res
             |        }""".stripMargin
      }.mkString("\n\t")
    }
         ${
      serviceReader.interface.map {
        case (functionName, ServiceInterface(inputs, outputType)) =>
          s"      runtime.unsafeRun(${functionName}Program.either) == Left(err)"
      }.mkString("\n\t")

    }
       |  }
       |}
 """.stripMargin

  }

}
