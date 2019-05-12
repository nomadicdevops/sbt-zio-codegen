package com.nomadicdevops.sbt.zio.codegen

import com.nomadicdevops.sbt.zio.codegen.readers._
import com.nomadicdevops.sbt.zio.codegen.templates.domain.{DomainGeneratorTemplate, DomainTemplate, GenericDomainGeneratorTemplate}
import com.nomadicdevops.sbt.zio.codegen.templates.enums.{EnumGeneratorTemplate, EnumTemplate}
import com.nomadicdevops.sbt.zio.codegen.templates.services.{ServiceImplTemplate, ServiceMockSpecTemplate, ServiceMockTemplate, ServiceStubTemplate, ServiceTemplate}
import com.nomadicdevops.sbt.zio.codegen.templates.util.PrimitiveTypesGeneratorTemplate
import com.nomadicdevops.sbt.zio.codegen.util.CodeGenUtil._
import com.nomadicdevops.sbt.zio.codegen.writers.{WriteGeneratedFile, WriteGeneratedImplFile, WriteTestFile}


object CodeGen {


  private def createEnumFile(appReader: AppReader, enumReader: EnumReader)
                            (implicit config: CodeGenConfig): Unit = {
    val contents = EnumTemplate(
      appReader = appReader,
      enumReader = enumReader
    )

    WriteGeneratedFile(appReader, enumReader.`type`, contents, Option("enums"))
  }

  private def createEnumGeneratorFile(appReader: AppReader, enumReader: EnumReader)
                                     (implicit config: CodeGenConfig): Unit = {
    val contents = EnumGeneratorTemplate(
      appReader = appReader,
      enumReader = enumReader
    )

    WriteTestFile(appReader, s"${enumReader.`type`}Gen", contents, Option("enum.generators"))
  }

  private def createServiceImplFile(appReader: AppReader, serviceReader: ServiceReader)
                                   (implicit config: CodeGenConfig): Unit = {
    val contents = ServiceImplTemplate(
      appReader = appReader,
      serviceReader = serviceReader
    )

    WriteGeneratedImplFile(appReader, serviceReader.`type`, contents, Option("services"))
  }

  private def createServiceMockFile(appReader: AppReader, serviceReader: ServiceReader)
                                   (implicit config: CodeGenConfig): Unit = {

    val contents = ServiceMockTemplate(
      appReader = appReader,
      serviceReader = serviceReader
    )

    WriteTestFile(appReader, s"Mock${serviceReader.`type`}", contents, Option("service.mocks"))
  }

  private def createServiceMockSpecsFile(appReader: AppReader, serviceReader: ServiceReader)
                                        (implicit config: CodeGenConfig): Unit = {
    val contents = ServiceMockSpecTemplate(
      appReader = appReader,
      serviceReader = serviceReader
    )

    WriteTestFile(appReader, s"Mock${serviceReader.`type`}Spec", contents, Option("service.mock.specs"))
  }

  private def createServiceStubFile(appReader: AppReader, serviceReader: ServiceReader)
                                   (implicit config: CodeGenConfig): Unit = {
    val contents = ServiceStubTemplate(
      appReader = appReader,
      serviceReader = serviceReader
    )
    WriteTestFile(appReader, s"Stub${serviceReader.`type`}", contents, Option("service.stubs"))
  }


  private def createServiceFile(appReader: AppReader, serviceReader: ServiceReader)
                               (implicit config: CodeGenConfig): Unit = {
    val contents = ServiceTemplate(
      appReader = appReader,
      serviceReader = serviceReader
    )

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
    val contents = PrimitiveTypesGeneratorTemplate(
      appReader = appReader
    )

    WriteTestFile(appReader, s"PrimitiveTypesGen", contents, Option("util"))
  }

  private def createGenericDomainGeneratorsFile(appReader: AppReader, domainReader: DomainReader)
                                               (implicit config: CodeGenConfig): Unit = {
    val contents = GenericDomainGeneratorTemplate(
      appReader = appReader,
      domainReader = domainReader
    )

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

