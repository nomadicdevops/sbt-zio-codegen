package com.nomadicdevops.sbt.zio.codegen.readers

case class AppReader(
                      packages: Packages,
                      error: String,
                      dependencies: List[String]
                    )

case class Packages(
                     generated: String,
                     impl: String
                   )