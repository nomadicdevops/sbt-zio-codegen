package com.nomadicdevops.sbt.zio.codegen

import com.nomadicdevops.sbt.zio.codegen.readers._
import com.nomadicdevops.sbt.zio.codegen.templates.domain.{DomainGeneratorTemplate, DomainTemplate}
import com.nomadicdevops.sbt.zio.codegen.templates.util.UtilGeneratorsTemplate
import com.nomadicdevops.sbt.zio.codegen.writers.{WriteGeneratedFile, WriteGeneratedImplFile, WriteTestFile}
import com.nomadicdevops.sbt.zio.codegen.util.CodeGenUtil._

import scala.util.Random


object CodeGen {


  private def createEnumFile(appReader: AppReader, enumReader: EnumReader)
                            (implicit config: CodeGenConfig): Unit = {
    val contents =
      s"""package ${appReader.packages.generated}.enums
         |
         |sealed trait ${enumReader.`type`}
        ${
        enumReader.subtypes.map(subtype =>
          subtype.fields match {
            case fields if fields.isEmpty => s"""case object ${subtype.`type`} extends ${enumReader.`type`}"""
            case fields => s"""
                              |case class ${subtype.`type`}(${
              fields.map { case (n, v) => s"\n\t$n: $v" }.mkString(",")
            }) extends ${enumReader.`type`}""".stripMargin
          }
        ).mkString("\n\n")
      }
       """.stripMargin

    WriteGeneratedFile(appReader, enumReader.`type`, contents, Option("enums"))
  }

  private def createEnumGeneratorFile(appReader: AppReader, enumReader: EnumReader)
                                     (implicit config: CodeGenConfig): Unit = {
    val all: String = enumReader.subtypes.map(subtype =>
      subtype.fields match {
        case fields if fields.isEmpty => subtype.`type`
        case fields => s"""${subtype.`type`}(${
          fields.map { case (n, _) =>
            s"""
               |        $n = $n""".stripMargin
          }.mkString(", \t")
        }
                          |       )""".stripMargin
      }
    ).mkString(",\n\t\t")

    val args: String = enumReader.subtypes.flatMap(subtype => subtype.fields.map {
      case (paramName, paramValue) => s"$paramName <- ${getGenerator(paramValue)}"
    }).mkString("\n\t")

    val contents =
      s"""package ${appReader.packages.generated}.enum.generators
         |
         |import org.scalacheck.Gen
         |import com.example.generated.enums._
         |
         |object ${enumReader.`type`}Gen {
         |  def apply(): Gen[${enumReader.`type`}] = for {
         |    $args
         |    enum <- Gen.oneOf(Seq(
         |      $all
         |    ))
         |  } yield enum
         |}
       """.stripMargin

    WriteTestFile(appReader, s"${enumReader.`type`}Gen", contents, Option("enum.generators"))
  }

  private def createServiceImplFile(appReader: AppReader, serviceReader: ServiceReader)
                                   (implicit config: CodeGenConfig): Unit = {
    val contents =
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

    WriteGeneratedImplFile(appReader, serviceReader.`type`, contents, Option("services"))
  }

  private def createServiceMockFile(appReader: AppReader, serviceReader: ServiceReader)
                                   (implicit config: CodeGenConfig): Unit = {

    val contents =
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

    WriteTestFile(appReader, s"Mock${serviceReader.`type`}", contents, Option("service.mocks"))
  }

  private def createServiceMockSpecsFile(appReader: AppReader, serviceReader: ServiceReader)
                                        (implicit config: CodeGenConfig): Unit = {
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

    val failureGenerators = (s"${appReader.error}Gen()" +: kvs2.map(a => stripGenericTypes(a._1))).mkString(",\n")
    val failureGeneratorsMatchingArgs = (s"err: ${appReader.error}" +: kvs2.map(a => stripGenericTypes(a._2))).mkString(",\n")


    val contents =
      s"""
         |package ${appReader.packages.generated}.service.mock.specs
         |
         |import com.example.generated.domain._
         |import com.example.generated.domain.generators._
         |import com.example.generated.enum.generators._
         |import com.example.generated.util._
         |import com.example.generated.enums._
         |import com.example.generated.service.mocks._
         |import com.example.generated.services._
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
               |      val ${functionName}Program: ZIO[ProgramEnv, ${appReader.error}, $outputType] =
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
               |      val ${functionName}Program: ZIO[ProgramEnv, ${appReader.error}, $outputType] =
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

    WriteTestFile(appReader, s"Mock${serviceReader.`type`}Spec", contents, Option("service.mock.specs"))
  }

  private def createServiceStubFile(appReader: AppReader, serviceReader: ServiceReader)
                                   (implicit config: CodeGenConfig): Unit = {

    val contents =
      s"""package ${appReader.packages.generated}.service.stubs
         |
         |import ${appReader.packages.generated}.services._
         |
         |object Stub${serviceReader.`type`} {
         |  def apply(
         |            serviceStub: ${serviceReader.`type`}.Service
         |           ): ${serviceReader.`type`} = new ${serviceReader.`type`} {
         |    override def ${makeFirstLetterLowerCase(serviceReader.`type`)}Service: ${serviceReader.`type`}.Service = serviceStub
         |  }
         |}
       """.stripMargin

    WriteTestFile(appReader, s"Stub${serviceReader.`type`}", contents, Option("service.stubs"))
  }

  private def makeFirstLetterLowerCase(s: String): String = {
    Character.toLowerCase(s.charAt(0)) + s.substring(1)
  }

  private def createServiceFile(appReader: AppReader, serviceReader: ServiceReader)
                               (implicit config: CodeGenConfig): Unit = {
    val contents =
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

    WriteGeneratedFile(appReader, serviceReader.`type`, contents, Option("services"))
  }

  private def createDomainFile(appReader: AppReader, domainReader: DomainReader)
                              (implicit config: CodeGenConfig): Unit = {

    val contents = DomainTemplate(
      appReader = appReader,
      domainReader = domainReader
    )

    WriteGeneratedFile(appReader, domainReader.`type`, contents, Option("domain"))
  }


  private def createDomainGeneratorsFile(appReader: AppReader, domainReader: DomainReader)
                                        (implicit config: CodeGenConfig): Unit = {
    val contents = DomainGeneratorTemplate(
      appReader = appReader,
      domainReader = domainReader
    )

    WriteTestFile(appReader, s"${domainReader.`type`}Gen", contents, Option("domain.generators"))
  }

  private def createUtilGeneratorsFile(appReader: AppReader)
                                      (implicit config: CodeGenConfig): Unit = {
    val contents = UtilGeneratorsTemplate(
        appReader = appReader
      )

    WriteTestFile(appReader, s"PrimitiveTypesGen", contents, Option("util"))
  }

  private def createGenericDomainGeneratorsFile(appReader: AppReader, domainReader: DomainReader)
                                               (implicit config: CodeGenConfig): Unit = {
    val contents =
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

    WriteTestFile(appReader, s"${stripGenericTypes(domainReader.`type`)}Gen", contents, Option("domain.generators"))
  }

  private def createAppFile(
                             appReader: AppReader
                           )(implicit config: CodeGenConfig): Unit = {

    val contents: String =
      s"""
         |package ${appReader.packages.impl}
         |
         |import scalaz.zio.internal.PlatformLive
         |import scalaz.zio.{Runtime, ZIO}
         |import ${appReader.packages.generated}.domain._
         |import ${appReader.packages.generated}.services._
         |import ${appReader.packages.generated}.enums._
         |import ${appReader.packages.impl}.services._
         |
         |object Main extends App {
         |
         |  type ProgramEnv = ${appReader.dependencies.mkString(" with ")}
         |  val programEnv: ProgramEnv = new ${appReader.dependencies.map(d => s"Live$d").mkString(" with ")} ${if (appReader.dependencies.size == 1) "{}" else ""}
         |
         |  val program: ZIO[ProgramEnv, ${appReader.error}, Unit] = ???
         |  /*
         |    for {
         |      _ <- ??? // TODO: implement me
         |    } yield {
         |      ()
         |    }
         |  */
         |
         |  val runtime: Runtime[ProgramEnv] = Runtime(programEnv, PlatformLive.Default)
         |
         |  runtime
         |  .unsafeRun(program.either)
         |  .fold(_ => 1, _ => 0)
         |
         |}
         |
       """.stripMargin

    WriteGeneratedImplFile(appReader, "Main", contents)

  }


  def createGeneratedFiles(implicit config: CodeGenConfig) = {

    val appReader: AppReader = getAppReader
    val serviceReaders: List[ServiceReader] = getReaders[ServiceReader](s"${config.apiDir}/services")
    val domainReaders: List[DomainReader] = getReaders[DomainReader](s"${config.apiDir}/domain")
    val enumsReaders: List[EnumReader] = getReaders[EnumReader](s"${config.apiDir}/enums")

    def isGeneric(t: String): Boolean = t.contains("[") && t.contains("]")

    createUtilGeneratorsFile(appReader)

    domainReaders
      .foreach(domainReader => {

        createDomainFile(appReader, domainReader)

        if (isGeneric(domainReader.`type`)) {
          createGenericDomainGeneratorsFile(appReader, domainReader)
        } else {
          createDomainGeneratorsFile(appReader, domainReader)
        }
      })

    serviceReaders
      .foreach(serviceReader => {
        createServiceFile(appReader, serviceReader)
        createServiceMockFile(appReader, serviceReader)
        createServiceStubFile(appReader, serviceReader)
        createServiceMockSpecsFile(appReader, serviceReader)
      })

    enumsReaders
      .foreach(enumReader => {
        createEnumFile(appReader, enumReader)
        createEnumGeneratorFile(appReader, enumReader)
      })
  }

  def createImplFiles(implicit config: CodeGenConfig) = {

    val appReader = getAppReader
    val serviceReaders: List[ServiceReader] = getReaders[ServiceReader](s"${config.apiDir}/services")

    createAppFile(appReader)

    serviceReaders
      .foreach(serviceReader => {
        createServiceImplFile(appReader, serviceReader)
      })
  }


}

