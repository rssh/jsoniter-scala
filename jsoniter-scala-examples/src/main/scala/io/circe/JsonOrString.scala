package io.circe

import com.github.plokhotnyuk.jsoniter_scala.core.{JsonReader, JsonValueCodec, JsonWriter}
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker
import io.circe.syntax.{EncoderOps, KeyOps}
import scala.collection.mutable

sealed abstract class JsonOrString
object JsonOrString {
  implicit val JsonCodec: JsonValueCodec[Json] = new JsonValueCodec[Json] {

    import scala.jdk.CollectionConverters._

    override def decodeValue(in: JsonReader, default: Json): Json = {
      var b = in.nextToken()
      if (b == '"') {
        in.rollbackToken()
        Json.fromString(in.readString(null))
      } else if (b == 'f' || b == 't') {
        in.rollbackToken()
        if (in.readBoolean()) Json.fromBoolean(true)
        else Json.fromBoolean(false)
      } else if (b == 'n') {
        in.readNullOrError(default, "expected `null` value")
        Json.Null
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
        if (i == l) Json.fromLong(in.readLong())
        else Json.fromDoubleOrString(in.readDouble())
      } else if (b == '{') {
        val obj = new java.util.LinkedHashMap[String, Json]
        if (!in.isNextToken('}')) {
          in.rollbackToken()
          do obj.put(in.readKeyAsString(), decodeValue(in, default)) while (in.isNextToken(','))
          if (!in.isCurrentToken('}')) in.objectEndOrCommaError()
        }
        Json.fromFields(obj.asScala)
      } else if (b == '[') {
        val arr = new mutable.ArrayBuffer[Json]
        if (!in.isNextToken(']')) {
          in.rollbackToken()
          do arr += decodeValue(in, default) while (in.isNextToken(','))
          if (!in.isCurrentToken(']')) in.arrayEndOrCommaError()
        }
        Json.fromValues(arr.toList)
      } else in.decodeError("expected JSON value")
    }

    override def encodeValue(x: Json, out: JsonWriter): Unit =
      x.fold(
        out.writeNull(),
        jsonBoolean => out.writeVal(jsonBoolean),
        {
          case JsonLong(l) => out.writeVal(l)
          case JsonDouble(d) => out.writeVal(d)
          case jsonNumber => out.encodeError("unsupported number: " + jsonNumber)
        },
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

    override def nullValue: Json = Json.Null
  }
  implicit val listOfJsonCodec: JsonValueCodec[List[Json]] = JsonCodecMaker.make[List[Json]]
  implicit val mapCodec: JsonValueCodec[Map[String, Json]] =
    JsonCodecMaker.makeWithoutDiscriminator
  implicit val jsonObjectCodec: JsonValueCodec[JsonObject] = new JsonValueCodec[JsonObject] {

    override def decodeValue(in: JsonReader, default: JsonObject): JsonObject = {
      var b = in.nextToken()
      if (b == '"') {
        in.rollbackToken()
        JsonObject.empty
      } else if (b == 'f' || b == 't') {
        in.rollbackToken()
        if (in.readBoolean()) Json.fromBoolean(true).asObject.getOrElse(JsonObject.empty)
        else Json.fromBoolean(false).asObject.getOrElse(JsonObject.empty)
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
        if (i == l) Json.fromLong(in.readLong()).asObject.getOrElse(JsonObject.empty)
        else Json.fromDoubleOrString(in.readDouble()).asObject.getOrElse(JsonObject.empty)
      } else if (b == '{') {
        val obj = scala.collection.mutable.Map[String, Json]()
        if (!in.isNextToken('}')) {
          in.rollbackToken()
          do obj.put(in.readKeyAsString(), JsonCodec.decodeValue(in, default.asJson)) while (in
            .isNextToken(','))
          if (!in.isCurrentToken('}')) in.objectEndOrCommaError()
        }
        JsonObject.fromIterable(obj)
      } else if (b == '[') {
        val arr = new mutable.ArrayBuffer[Json]
        if (!in.isNextToken(']')) {
          in.rollbackToken()
          do arr += JsonCodec.decodeValue(in, Json.fromFields(default.toMap)) while (in
            .isNextToken(','))
          if (!in.isCurrentToken(']')) in.arrayEndOrCommaError()
        }

        Json.fromValues(arr).asObject.getOrElse(JsonObject.empty)
      } else in.decodeError("expected JSON value")
    }

    override def encodeValue(x: JsonObject, out: JsonWriter): Unit =
      mapCodec.encodeValue(x.toMap, out)

    override def nullValue: JsonObject = JsonObject.empty
  }
  case class Str(s: String) extends JsonOrString

  case class Js(j: Either[JsonObject, List[Json]]) extends JsonOrString

}
