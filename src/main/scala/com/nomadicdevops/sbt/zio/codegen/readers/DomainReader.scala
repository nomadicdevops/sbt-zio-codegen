package com.nomadicdevops.sbt.zio.codegen.readers

case class DomainReader(
                         `type`: String,
                         fields: Map[String, String]
                       )