package com.nomadicdevops.sbt.zio.codegen.util

import scala.util.Random

object CodeGenUtil {

  def stripGenericTypes(s: String): String = s.replaceAll("\\[(.*?)\\]", "")

  def accountForFunctionType(s: String): String = s.replaceAll("=>", "_")

  def getGenericTypes(s: String): String = s.replaceAll("^.+?(?=\\[)", "")

  def getGenericTypesAsList(s: String): List[String] =
    s.replaceAll("^.+?(?=\\[)", "")
      .replaceAll("\\[", "")
      .replaceAll("\\]", "")
      .split(",")
      .toList

  def getGenerator(fieldType: String): String = fieldType match {
    case "String" => "Gen.alphaStr"
    case "Int" | "Double" | "Float" => {
      val min: Int = new Random().nextInt(100)
      val max: Int = min * 20
      s"Gen.choose($min, $max)"
    }
    case _ => "???" //TODO: implement as need be
  }

}
