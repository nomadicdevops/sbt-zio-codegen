package com.nomadicdevops.sbt.zio.codegen

import com.nomadicdevops.sbt.zio.codegen.readers.{AppConfig, Packages, ServiceReader, getReaders}
import com.nomadicdevops.sbt.zio.codegen.util.CodeGenUtil.stripGenericTypes

case class CodeGenConfig(
                          apiDir: String,
                          srcMainScalaDir: String,
                          srcTestScalaDir: String,
                          appConfig: AppConfig
                        )

object CodeGenConfig {
  def apply(
             apiDir: String,
             srcMainScalaDir: String,
             srcTestScalaDir: String,
             zioCodeGenGeneratedPackageName: String,
             zioCodeGenGeneratedImplPackageName: String,
             zioCodeGenErrorType: String,
             servicesDir: String
           ): CodeGenConfig =
    CodeGenConfig(
      apiDir = apiDir,
      srcMainScalaDir = srcMainScalaDir,
      srcTestScalaDir = srcTestScalaDir,
      appConfig = AppConfig(
        packages = Packages(
          generated = zioCodeGenGeneratedPackageName,
          impl = zioCodeGenGeneratedImplPackageName
        ),
        error = zioCodeGenErrorType,
        dependencies = getReaders[ServiceReader](
          path = s"$servicesDir/services"
        ).map(
          serviceReader => stripGenericTypes(
            serviceReader.`type`
          )
        )
      ))

}