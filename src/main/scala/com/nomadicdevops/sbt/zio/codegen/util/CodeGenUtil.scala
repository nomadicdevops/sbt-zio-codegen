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

  def getGenerator(fieldType: String): String = s"${fieldType}Gen()"


  def makeFirstLetterLowerCase(s: String): String = {
    Character.toLowerCase(s.charAt(0)) + s.substring(1)
  }

  def isGeneric(t: String): Boolean = t.contains("[") && t.contains("]")

}
