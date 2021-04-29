package com.test

import com.github.plokhotnyuk.jsoniter_scala.core.{readFromArray, writeToArray}
import io.circe.JsonOrString._
import com.test.Text.textCodec
import io.circe.{Json, JsonObject}
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import io.circe.syntax.EncoderOps
import org.scalatest.{Matchers, WordSpec}
import com.test.LogRecord._

class codecTest extends WordSpec with Matchers {

  "decoding text " should {

    "parse text in case string value" in {
      val inputText = """{"text":"hello this is a test"}""".stripMargin

      val expectedText = Text(Str("hello this is a test"))
      val text: Text = readFromArray(inputText.getBytes("UTF-8"))(textCodec)
      val json: Array[Byte] = writeToArray(expectedText)
      text shouldBe expectedText
      new String(json, "UTF-8") shouldBe inputText
    }
    "parse text in case jsonObject value" in {
      val inner = Map[String,Json]("someValue" -> "hello".asJson, "anotherValue" -> 1.asJson, "justAnother" -> 2.0.asJson)
      val outer = Map[String,Json]("text" -> inner.asJson)
      val jObject: JsonObject = JsonObject.fromMap(outer)
      val objectStr: String = jObject.asJson.noSpaces
      val expectedText: Text = Text(Js(Left(jObject)))

      val text: Text = readFromArray(objectStr.getBytes("UTF-8"))(textCodec)
      val json: Array[Byte] = writeToArray(expectedText)
      text shouldBe expectedText
      new String(json, "UTF-8") shouldBe objectStr
    }

    "parse text in case list[json] value" in {
      val inner = Map[String,Json]("hello" -> 5.asJson).asJson
      val list = List[Json](inner,inner,inner,inner,inner)
      val listStr: String = Json.fromValues(list).noSpaces
      val expectedText: Text = Text(Js(Right(list)))
      val text: Text = readFromArray(listStr.getBytes("UTF-8"))(textCodec)
      val json: Array[Byte] = writeToArray(expectedText)
      text shouldBe expectedText
      new String(json, "UTF-8") shouldBe listStr
    }
  }

  "decoding logRecord " should {

    "decode threadID as long" in {
      val logRecord: LogRecord = LogRecord(Some(Left(1)))
      val logRecordText: String = logRecord.asJson(LogRecord.encoder).noSpaces

      val logRecordTest: LogRecord = readFromArray(logRecordText.getBytes("UTF-8"))(logRecordCodec)
      val logRecordJson: Array[Byte] = writeToArray(logRecord)(logRecordCodec)
      logRecordTest shouldBe logRecord
      new String(logRecordJson, "UTF-8") shouldBe logRecordText
    }

    "decode threadID as String" in {
      val logRecord: LogRecord = LogRecord(Some(Right("5")))
      val logRecordText: String = logRecord.asJson(LogRecord.encoder).noSpaces

      val logRecordTest: LogRecord = readFromArray(logRecordText.getBytes("UTF-8"))(logRecordCodec)
      val logRecordJson: Array[Byte] = writeToArray(logRecord)(logRecordCodec)
      logRecordTest shouldBe logRecord
      new String(logRecordJson, "UTF-8") shouldBe logRecordText
    }

    "decode threadID as null" in {
      val logRecord: LogRecord = LogRecord(None)
      val logRecordText: String = logRecord.asJson(LogRecord.encoder).noSpaces

      val logRecordTest: LogRecord = readFromArray(logRecordText.getBytes("UTF-8"))(logRecordCodec)
      val logRecordJson: Array[Byte] = writeToArray(logRecord)(logRecordCodec)
      logRecordTest shouldBe logRecord
      new String(logRecordJson, "UTF-8") shouldBe logRecordText
    }
  }
}
