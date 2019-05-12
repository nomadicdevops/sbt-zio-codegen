package com.nomadicdevops.sbt.zio.codegen

import com.nomadicdevops.sbt.zio.codegen.readers._
import com.nomadicdevops.sbt.zio.codegen.templates.MainTemplate
import com.nomadicdevops.sbt.zio.codegen.templates.domain.{DomainGeneratorTemplate, DomainTemplate, GenericDomainGeneratorTemplate}
import com.nomadicdevops.sbt.zio.codegen.templates.enums.{EnumGeneratorTemplate, EnumTemplate}
import com.nomadicdevops.sbt.zio.codegen.templates.services.{ServiceImplTemplate, ServiceMockSpecTemplate, ServiceMockTemplate, ServiceStubTemplate, ServiceTemplate}
import com.nomadicdevops.sbt.zio.codegen.templates.util.PrimitiveTypesGeneratorTemplate
import com.nomadicdevops.sbt.zio.codegen.util.CodeGenUtil._
import com.nomadicdevops.sbt.zio.codegen.writers.{WriteGeneratedFile, WriteGeneratedImplFile, WriteTestFile}

object CodeGen {

  def createGeneratedFiles(implicit config: CodeGenConfig): Unit = {

    val appReader: AppReader = getAppReader
    val serviceReaders: List[ServiceReader] = getReaders[ServiceReader](s"${config.apiDir}/services")
    val domainReaders: List[DomainReader] = getReaders[DomainReader](s"${config.apiDir}/domain")
    val enumsReaders: List[EnumReader] = getReaders[EnumReader](s"${config.apiDir}/enums")

    WriteTestFile(
      appReader = appReader,
      scalaClass = "PrimitiveTypesGen",
      contents = PrimitiveTypesGeneratorTemplate(
        appReader = appReader
      ),
      subPackage = Option("util")
    )

    domainReaders
      .foreach(domainReader => {

        WriteGeneratedFile(
          appReader = appReader,
          scalaClass = domainReader.`type`,
          contents = DomainTemplate(
            appReader = appReader,
            domainReader = domainReader
          ),
          subPackage = Option("domain")
        )

        if (isGeneric(domainReader.`type`)) {

          WriteTestFile(
            appReader = appReader,
            scalaClass = s"${stripGenericTypes(domainReader.`type`)}Gen",
            contents = GenericDomainGeneratorTemplate(
              appReader = appReader,
              domainReader = domainReader
            ), subPackage = Option("domain.generators")
          )

        } else {

          WriteTestFile(
            appReader = appReader,
            scalaClass = s"${domainReader.`type`}Gen",
            contents = DomainGeneratorTemplate(
              appReader = appReader,
              domainReader = domainReader
            ),
            subPackage = Option("domain.generators")
          )

        }
      })

    serviceReaders
      .foreach(serviceReader => {

        WriteGeneratedFile(
          appReader = appReader,
          scalaClass = serviceReader.`type`,
          contents = ServiceTemplate(
            appReader = appReader,
            serviceReader = serviceReader
          ),
          subPackage = Option("services")
        )

        WriteTestFile(
          appReader = appReader,
          scalaClass = s"Mock${serviceReader.`type`}",
          contents = ServiceMockTemplate(
            appReader = appReader,
            serviceReader = serviceReader
          ),
          subPackage = Option("service.mocks")
        )

        WriteTestFile(
          appReader = appReader,
          scalaClass = s"Stub${serviceReader.`type`}",
          contents = ServiceStubTemplate(
            appReader = appReader,
            serviceReader = serviceReader
          ),
          subPackage = Option("service.stubs")
        )

        WriteTestFile(
          appReader = appReader,
          scalaClass = s"Mock${serviceReader.`type`}Spec",
          contents = ServiceMockSpecTemplate(
            appReader = appReader,
            serviceReader = serviceReader
          ),
          subPackage = Option("service.mock.specs")
        )
      })

    enumsReaders
      .foreach(enumReader => {

        WriteGeneratedFile(
          appReader = appReader,
          scalaClass = enumReader.`type`,
          contents = EnumTemplate(
            appReader = appReader,
            enumReader = enumReader
          ),
          subPackage = Option("enums")
        )

        WriteTestFile(
          appReader = appReader,
          scalaClass = s"${enumReader.`type`}Gen",
          contents = EnumGeneratorTemplate(
            appReader = appReader,
            enumReader = enumReader
          ),
          subPackage = Option("enum.generators")
        )

      })
  }

  def createImplFiles(implicit config: CodeGenConfig): Unit = {

    val appReader = getAppReader
    val serviceReaders: List[ServiceReader] = getReaders[ServiceReader](s"${config.apiDir}/services")

    WriteGeneratedImplFile(
      appReader = appReader,
      scalaClass = "Main",
      contents = MainTemplate(
        appReader = appReader
      )
    )

    serviceReaders
      .foreach(serviceReader => {

        WriteGeneratedImplFile(
          appReader = appReader,
          scalaClass = serviceReader.`type`,
          contents = ServiceImplTemplate(
            appReader = appReader,
            serviceReader = serviceReader
          ),
          subPackage = Option("services")
        )

      })
  }


}

