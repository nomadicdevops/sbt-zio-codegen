package com.nomadicdevops.sbt.zio.codegen.readers

case class AppConfig(
                      packages: Packages,
                      error: String,
                      dependencies: List[String]
                    )

case class Packages(
                     generated: String,
                     impl: String
                   )