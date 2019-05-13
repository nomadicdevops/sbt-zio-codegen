package com.nomadicdevops.sbt.zio.codegen.templates.util

import com.nomadicdevops.sbt.zio.codegen.readers.AppConfig

object GenHelperTemplate {

  def apply(
             appConfig: AppConfig
           ): String = {
    s"""
       |package ${appConfig.packages.generated}.util
       |
       |import org.scalacheck.Gen
       |
       |object UnitGen { def apply(): Gen[Unit] = Gen.oneOf(Seq(())) }
       |object StringGen { def apply(): Gen[String] = Gen.alphaStr }
       |object IntGen { def apply(): Gen[Int] = Gen.choose(0, 100) } //TODO: update
       |object DoubleGen { def apply(): Gen[Double] = Gen.choose(0, 100) } //TODO: update
       |object ThrowableGen { def apply(): Gen[Throwable] = Gen.oneOf(Seq(new Exception())) }
       """.stripMargin
  }

}
