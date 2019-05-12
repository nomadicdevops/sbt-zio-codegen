package com.nomadicdevops.sbt.zio.codegen.templates.services

import com.nomadicdevops.sbt.zio.codegen.readers.{AppReader, ServiceReader}
import com.nomadicdevops.sbt.zio.codegen.util.CodeGenUtil.makeFirstLetterLowerCase

object ServiceStubTemplate {

  def apply(
             appReader: AppReader,
             serviceReader: ServiceReader
           ): String = {

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

  }

}
