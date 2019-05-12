package com.nomadicdevops.sbt.zio.codegen

import java.io.File

import com.github.plokhotnyuk.jsoniter_scala.core.{JsonValueCodec, readFromArray}
import com.github.plokhotnyuk.jsoniter_scala.macros.{CodecMakerConfig, JsonCodecMaker}

import scala.io.Source

package object readers {
  implicit val domainReaderCodec: JsonValueCodec[DomainReader] = JsonCodecMaker.make[DomainReader](CodecMakerConfig())
  implicit val appReaderCodec: JsonValueCodec[AppReader] = JsonCodecMaker.make[AppReader](CodecMakerConfig())
  implicit val serviceReaderCodec: JsonValueCodec[ServiceReader] = JsonCodecMaker.make[ServiceReader](CodecMakerConfig())
  implicit val enumReaderCodec: JsonValueCodec[EnumReader] = JsonCodecMaker.make[EnumReader](CodecMakerConfig())

  def getReaders[A](path: String)(implicit codec: JsonValueCodec[A]): List[A] = {
    val folder = new File(path)
    if (folder.exists && folder.isDirectory) {
      folder
        .listFiles
        .toList
        .filter(f => f.getName.endsWith(".json"))
        .map(parse[A])
    } else Nil
  }

  def getAppReader(implicit config: CodeGenConfig): AppReader =
    parse[AppReader](new File(s"${config.apiDir}/app.json"))

  private def parse[A](file: File)(implicit codec: JsonValueCodec[A]): A = {
    val json = Source.fromFile(file).getLines.mkString.replaceAll(" ", "")
    readFromArray[A](json.getBytes("UTF-8"))
  }

}
