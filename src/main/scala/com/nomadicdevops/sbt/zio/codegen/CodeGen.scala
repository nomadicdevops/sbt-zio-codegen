package com.nomadicdevops.sbt.zio.codegen

import com.nomadicdevops.sbt.zio.codegen.readers._
import com.nomadicdevops.sbt.zio.codegen.templates.MainTemplate
import com.nomadicdevops.sbt.zio.codegen.templates.domain.{DomainGeneratorTemplate, DomainTemplate, GenericDomainGeneratorTemplate}
import com.nomadicdevops.sbt.zio.codegen.templates.enums.{EnumGeneratorTemplate, EnumTemplate}
import com.nomadicdevops.sbt.zio.codegen.templates.services.{ServiceImplTemplate, ServiceMockSpecTemplate, ServiceMockTemplate, ServiceStubTemplate, ServiceTemplate}
import com.nomadicdevops.sbt.zio.codegen.templates.util.GenHelperTemplate
import com.nomadicdevops.sbt.zio.codegen.util.CodeGenUtil._
import com.nomadicdevops.sbt.zio.codegen.writers.{WriteGeneratedFile, WriteGeneratedImplFile, WriteTestFile}

object CodeGen {

  def createGeneratedFiles(implicit config: CodeGenConfig): Unit = {

    val appConfig: AppConfig = config.appConfig
    val serviceReaders: List[ServiceReader] = getReaders[ServiceReader](s"${config.apiDir}/services")
    val domainReaders: List[DomainReader] = getReaders[DomainReader](s"${config.apiDir}/domain")
    val enumsReaders: List[EnumReader] = getReaders[EnumReader](s"${config.apiDir}/enums")

    WriteTestFile(
      appConfig = appConfig,
      scalaClass = "GenHelper",
      contents = GenHelperTemplate(
        appConfig = appConfig
      ),
      subPackage = Option("util")
    )

    domainReaders
      .foreach(domainReader => {

        WriteGeneratedFile(
          appConfig = appConfig,
          scalaClass = domainReader.`type`,
          contents = DomainTemplate(
            appConfig = appConfig,
            domainReader = domainReader
          ),
          subPackage = Option("domain")
        )

        if (isGeneric(domainReader.`type`)) {

          WriteTestFile(
            appConfig = appConfig,
            scalaClass = s"${stripGenericTypes(domainReader.`type`)}Gen",
            contents = GenericDomainGeneratorTemplate(
              appConfig = appConfig,
              domainReader = domainReader
            ), subPackage = Option("domain.generators")
          )

        } else {

          WriteTestFile(
            appConfig = appConfig,
            scalaClass = s"${domainReader.`type`}Gen",
            contents = DomainGeneratorTemplate(
              appConfig = appConfig,
              domainReader = domainReader
            ),
            subPackage = Option("domain.generators")
          )

        }
      })

    serviceReaders
      .foreach(serviceReader => {

        WriteGeneratedFile(
          appConfig = appConfig,
          scalaClass = serviceReader.`type`,
          contents = ServiceTemplate(
            appConfig = appConfig,
            serviceReader = serviceReader
          ),
          subPackage = Option("services")
        )

        WriteTestFile(
          appConfig = appConfig,
          scalaClass = s"Mock${serviceReader.`type`}",
          contents = ServiceMockTemplate(
            appConfig = appConfig,
            serviceReader = serviceReader
          ),
          subPackage = Option("service.mocks")
        )

        WriteTestFile(
          appConfig = appConfig,
          scalaClass = s"Stub${serviceReader.`type`}",
          contents = ServiceStubTemplate(
            appConfig = appConfig,
            serviceReader = serviceReader
          ),
          subPackage = Option("service.stubs")
        )

        WriteTestFile(
          appConfig = appConfig,
          scalaClass = s"Mock${serviceReader.`type`}Spec",
          contents = ServiceMockSpecTemplate(
            appConfig = appConfig,
            serviceReader = serviceReader
          ),
          subPackage = Option("service.mock.specs")
        )
      })

    enumsReaders
      .foreach(enumReader => {

        WriteGeneratedFile(
          appConfig = appConfig,
          scalaClass = enumReader.`type`,
          contents = EnumTemplate(
            appConfig = appConfig,
            enumReader = enumReader
          ),
          subPackage = Option("enums")
        )

        WriteTestFile(
          appConfig = appConfig,
          scalaClass = s"${enumReader.`type`}Gen",
          contents = EnumGeneratorTemplate(
            appConfig = appConfig,
            enumReader = enumReader
          ),
          subPackage = Option("enum.generators")
        )

      })
  }

  def createImplFiles(implicit config: CodeGenConfig): Unit = {

    val appConfig = config.appConfig
    val serviceReaders: List[ServiceReader] = getReaders[ServiceReader](s"${config.apiDir}/services")

    WriteGeneratedImplFile(
      appConfig = appConfig,
      scalaClass = "Main",
      contents = MainTemplate(
        appConfig = appConfig
      )
    )

    serviceReaders
      .foreach(serviceReader => {

        WriteGeneratedImplFile(
          appConfig = appConfig,
          scalaClass = serviceReader.`type`,
          contents = ServiceImplTemplate(
            appConfig = appConfig,
            serviceReader = serviceReader
          ),
          subPackage = Option("services")
        )

      })
  }


}

