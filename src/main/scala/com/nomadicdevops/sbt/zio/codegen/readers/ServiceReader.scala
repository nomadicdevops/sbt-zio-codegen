package com.nomadicdevops.sbt.zio.codegen.readers

case class ServiceReader(
                          `type`: String,
                          interface: Map[String, ServiceInterface]
                        )

case class ServiceInterface(
                     inputs: Map[String, String],
                     output: String
                   )

