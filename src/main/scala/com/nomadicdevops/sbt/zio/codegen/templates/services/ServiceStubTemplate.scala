package com.nomadicdevops.sbt.zio.codegen.templates.services

import com.nomadicdevops.sbt.zio.codegen.readers.{AppConfig, ServiceReader}
import com.nomadicdevops.sbt.zio.codegen.util.CodeGenUtil.makeFirstLetterLowerCase

object ServiceStubTemplate {

  def apply(
             appConfig: AppConfig,
             serviceReader: ServiceReader
           ): String = {

      s"""package ${appConfig.packages.generated}.service.stubs
         |
         |import ${appConfig.packages.generated}.services._
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
