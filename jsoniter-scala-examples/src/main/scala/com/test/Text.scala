package com.test

import com.github.plokhotnyuk.jsoniter_scala.core.{JsonReader, JsonValueCodec, JsonWriter}
import com.github.plokhotnyuk.jsoniter_scala.macros.{CodecMakerConfig, JsonCodecMaker, named}
import com.test.JsonOrString.{Js, Str}
import io.circe.syntax.EncoderOps
import io.circe.{JsonObject, Json => CirceJson}

import scala.annotation.switch
import scala.collection.mutable
import scala.jdk.CollectionConverters._

case class Bulk(
  @named("subsystemName")
  subSystem: SubSystem,
  applicationName: AppName,
  privateKey: PrivateKey, // Add value
  computerName: Option[ComputerName], // Add value
  logEntries: List[LogRecord],
  @named("es_cluster")
  es_cluster: Option[EsCluster], // Add Value
  sdk: SDK,
  @named("SDKId")
  sdkId: Option[SdkId]
)

case class LogRecord(
  category: Option[Category],
  className: Option[ClassName],
  methodName: Option[MethodName],
  severity: Severity,
  @named("threadId")
  thread: Option[Either[Long, String]],
  timestamp: Timestamp,
  logSeed: LogSeed,
  text: Text,
  logId: LogId
)

case class CompanyId(id: Int)

case class AppName(value: String) extends AnyVal

case class SubSystem(value: String) extends AnyVal

case class Category(value: String) extends AnyVal

case class ClassName(value: String) extends AnyVal

case class MethodName(value: String) extends AnyVal

case class Severity(value: Int) extends AnyVal

case class Timestamp(value: Long) extends AnyVal

case class LogSeed(value: Long) extends AnyVal

case class FieldName(value: String) extends AnyVal {
  override def toString: String = value
  def splitValue: Array[String] = value.split('.')
}

case class Text(value: JsonOrString) extends AnyVal

case class PrivateKey(value: String) extends AnyVal

case class ComputerName(value: String) extends AnyVal

case class EsCluster(value: String) extends AnyVal

case class SdkId(value: String) extends AnyVal

case class Version(value: String) extends AnyVal

case class IPAddress(value: String) extends AnyVal

case class ProcessName(value: String) extends AnyVal

case class BulkSeedIdx(value: Long) extends AnyVal

case class SDK(
  version: Option[Version],
  bulkSeed: (BulkSeedIdx, BulkSeedIdx),
  companyId: CompanyId,
  applicationName: AppName,
  @named("subsystemName")
  subSystem: SubSystem,
  computerName: Option[ComputerName],
  IPAddress: Option[IPAddress],
  id: Option[SdkId],
  @named("es_cluster")
  esCluster: Option[EsCluster],
  processName: Option[ProcessName]
)

sealed abstract class JsonOrString
object JsonOrString {
  case class Str(s: String) extends JsonOrString

  case class Js(j: Either[JsonObject, List[CirceJson]]) extends JsonOrString
}

case class Op(value: String) extends AnyVal

case class LogId(value: String) extends AnyVal

object Codecs {
  implicit val eitherLongOrStringCodec: JsonValueCodec[Either[Long, String]] =
    new JsonValueCodec[Either[Long, String]] {
      override def decodeValue(in: JsonReader, default: Either[Long, String]): Either[Long, String] = {
        val t = in.nextToken()
        in.rollbackToken()
        if (t == '"') Right(in.readString(null))
        else Left(in.readLong())
      }

      override def encodeValue(x: Either[Long, String], out: JsonWriter): Unit =
        x match {
          case Right(s) => out.writeVal(s)
          case Left(l)  => out.writeVal(l)
        }

      override def nullValue: Either[Long, String] = null
    }
  implicit val companyIdCodec: JsonValueCodec[CompanyId] =
    new JsonValueCodec[CompanyId] {
      override def decodeValue(in: JsonReader, default: CompanyId): CompanyId = {
        val t = in.nextToken()
        in.rollbackToken()
        if (t == '"') in.requiredFieldError("id is a required field")
        else CompanyId(in.readInt())
      }

      override def encodeValue(x: CompanyId, out: JsonWriter): Unit =
        out.writeVal(x.id)

      override def nullValue: CompanyId = null
    }
  implicit val textCodec: JsonValueCodec[Text] = new JsonValueCodec[Text] {
    val nullValue: Text = Text(Str(""))

    def decodeValue(in: JsonReader, default: Text): Text =
      (in.nextToken(): @switch) match {
        case '{' =>
          in.rollbackToken()
          Text(Js(Left(jsonObjectCodec.decodeValue(in, jsonObjectCodec.nullValue))))
        case '[' =>
          in.rollbackToken()
          Text(Js(Right(listOfJsonCodec.decodeValue(in, listOfJsonCodec.nullValue))))
        case '"' =>
          in.rollbackToken()
          Text(Str(in.readString(null)))
        case _ =>
          in.decodeError("expected '{' or '\"'")
      }

    def encodeValue(x: Text, out: JsonWriter): _root_.scala.Unit =
      x.value match {
        case str: Str =>
          out.writeVal(str.s)
        case Js(j) =>
          j match {
            case Left(jsonObject) =>
              jsonObjectCodec.encodeValue(jsonObject, out)
            case Right(listOfJson) =>
              listOfJsonCodec.encodeValue(listOfJson, out)
          }
      }
  }
  implicit val jsonCodec: JsonValueCodec[CirceJson] =
    new JsonValueCodec[CirceJson] {

      import scala.jdk.CollectionConverters._

      override def decodeValue(in: JsonReader,
                               default: CirceJson): CirceJson = {
        var b = in.nextToken()
        if (b == '"') {
          in.rollbackToken()
          CirceJson.fromString(in.readString(null))
        } else if (b == 'f' || b == 't') {
          in.rollbackToken()
          if (in.readBoolean()) CirceJson.fromBoolean(true)
          else CirceJson.fromBoolean(false)
        } else if (b == 'n') {
          in.readNullOrError(default, "expected `null` value")
          CirceJson.Null
        } else if ((b >= '0' && b <= '9') || b == '-') {
          in.rollbackToken()
          in.setMark()
          val bs = in.readRawValAsBytes()
          val l = bs.length
          var i = 0
          while (i < l && {
            b = bs(i)
            (b >= '0' && b <= '9') || b == '-'
          }) i += 1
          in.rollbackToMark()
          if (i == l) CirceJson.fromLong(in.readLong())
          else CirceJson.fromBigDecimal(in.readBigDecimal(null))
        } else if (b == '{') {
          val obj = new java.util.LinkedHashMap[String, CirceJson]
          if (!in.isNextToken('}')) {
            in.rollbackToken()
            do obj.put(in.readKeyAsString(), decodeValue(in, default)) while (in
              .isNextToken(','))
            if (!in.isCurrentToken('}')) in.objectEndOrCommaError()
          }
          CirceJson.fromFields(obj.asScala)
        } else if (b == '[') {
          val arr = new mutable.ArrayBuffer[CirceJson]
          if (!in.isNextToken(']')) {
            in.rollbackToken()
            do arr += decodeValue(in, default) while (in.isNextToken(','))
            if (!in.isCurrentToken(']')) in.arrayEndOrCommaError()
          }
          CirceJson.fromValues(arr.toList)
        } else in.decodeError("expected JSON value")
      }

      override def encodeValue(x: CirceJson, out: JsonWriter): Unit =
        x.fold(
          out.writeNull(),
          jsonBoolean => out.writeVal(jsonBoolean),
          jsonNumber =>
            io.circe.JsonNumberOps.JsonNumberOps(jsonNumber).printNumber(out),
          jsonString => out.writeVal(jsonString),
          jsonArray => {
            out.writeArrayStart()
            jsonArray.foreach(v => encodeValue(v, out))
            out.writeArrayEnd()
          },
          jsonObject => {
            out.writeObjectStart()
            jsonObject.toIterable.foreach {
              case (k, v) =>
                out.writeKey(k)
                encodeValue(v, out)
            }
            out.writeObjectEnd()
          }
        )

      override def nullValue: CirceJson = CirceJson.Null
    }
  implicit val listOfJsonCodec: JsonValueCodec[List[CirceJson]] =
    JsonCodecMaker.make[List[CirceJson]]
  implicit val jsonObjectCodec: JsonValueCodec[JsonObject] =
    new JsonValueCodec[JsonObject] {

      override def decodeValue(in: JsonReader,
                               default: JsonObject): JsonObject = {
        var b = in.nextToken()
        if (b == '"') {
          in.rollbackToken()
          JsonObject.empty
        } else if (b == 'f' || b == 't') {
          in.rollbackToken()
          if (in.readBoolean())
            CirceJson.fromBoolean(true).asObject.getOrElse(JsonObject.empty)
          else CirceJson.fromBoolean(false).asObject.getOrElse(JsonObject.empty)
        } else if (b == 'n') {
          in.readNullOrError(default, "expected `null` value")
          JsonObject.empty
        } else if ((b >= '0' && b <= '9') || b == '-') {
          in.rollbackToken()
          in.setMark()
          val bs = in.readRawValAsBytes()
          val l = bs.length
          var i = 0
          while (i < l && {
            b = bs(i)
            (b >= '0' && b <= '9') || b == '-'
          }) i += 1
          in.rollbackToMark()
          if (i == l) CirceJson.fromLong(in.readLong()).asObject.getOrElse(JsonObject.empty)
          else CirceJson.fromBigDecimal(in.readDouble()).asObject.getOrElse(JsonObject.empty)
        } else if (b == '{') {
          val obj = new java.util.LinkedHashMap[String, CirceJson]
          if (!in.isNextToken('}')) {
            in.rollbackToken()
            do obj.put(in.readKeyAsString(), jsonCodec.decodeValue(in, default.asJson))
            while (in.isNextToken(','))
            if (!in.isCurrentToken('}')) in.objectEndOrCommaError()
          }
          JsonObject.fromIterable(obj.asScala)
        } else if (b == '[') {
          val arr = new mutable.ArrayBuffer[CirceJson]
          if (!in.isNextToken(']')) {
            in.rollbackToken()
            do arr += jsonCodec.decodeValue(in, CirceJson.fromFields(default.toMap))
            while (in.isNextToken(','))
            if (!in.isCurrentToken(']')) in.arrayEndOrCommaError()
          }
          CirceJson.fromValues(arr).asObject.getOrElse(JsonObject.empty)
        } else in.decodeError("expected JSON value")
      }

      override def encodeValue(x: JsonObject, out: JsonWriter): Unit = {
        out.writeObjectStart()
        x.toIterable.foreach {
          case (k, v: CirceJson) =>
            out.writeKey(k)
            jsonCodec.encodeValue(v, out)
        }
        out.writeObjectEnd()
      }

      override def nullValue: JsonObject = JsonObject.empty
    }
  implicit val bulkCodec: JsonValueCodec[Bulk] =
    JsonCodecMaker.make(CodecMakerConfig.withTransientNone(false))
}

