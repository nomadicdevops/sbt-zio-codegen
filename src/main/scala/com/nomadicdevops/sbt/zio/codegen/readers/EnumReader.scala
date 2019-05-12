package com.nomadicdevops.sbt.zio.codegen.readers

case class EnumReader(
                       `type`: String,
                       subtypes: List[Subtype]
                     )

case class Subtype(
                    `type`: String,
                    fields: Map[String, String]
                  )