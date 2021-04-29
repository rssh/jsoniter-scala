package com.test

import com.github.plokhotnyuk.jsoniter_scala.core.{JsonReader, JsonValueCodec, JsonWriter}
import io.circe.JsonOrString
import io.circe.JsonOrString.{Js, Str}
import io.circe.{Json, JsonObject}

import scala.annotation.switch

case class Text(value: JsonOrString) extends AnyVal
object Text {
  implicit val textCodec: JsonValueCodec[Text] = new JsonValueCodec[Text] {
    val nullValue: Text = Text(Str(""))

    def decodeValue(in: JsonReader, default: Text): Text =
      (in.nextToken(): @switch) match {
        case '{' =>
          var result: Text = nullValue
          var p0 = 0x1
          if (!in.isNextToken('}')) {
            in.rollbackToken()
            var l = -1
            while (l < 0 || in.isNextToken(',')) {
              l = in.readKeyAsCharBuf()
              if (in.isCharBufEqualsTo(l, "text")) {
                if ((p0 & 0x1) != 0) p0 ^= 0x1
                else in.duplicatedKeyError(l)
                (in.nextToken(): @switch) match {
                  case '{' =>
                    in.rollbackToken()
                    result = Text(Js(Left(JsonObject.fromMap(Map[String,Json]("text" -> JsonOrString.JsonCodec.decodeValue(in, JsonOrString.JsonCodec.nullValue))))))
                  case '"' =>
                    in.rollbackToken()
                    result = Text(Str(in.readString(null)))
                  case _ =>
                    in.decodeError("expected '{' or '\"'")
                }
              } else in.skip()
            }
            if (!in.isCurrentToken('}')) in.objectEndOrCommaError()
          }
          if ((p0 & 0x1) != 0) in.requiredFieldError("text")
          result
        case '[' =>
          in.rollbackToken()
          Text(Js(Right(JsonOrString.listOfJsonCodec.decodeValue(in, JsonOrString.listOfJsonCodec.nullValue))))
        case _ =>
          in.decodeError("expected '{' or '['")
      }

    def encodeValue(x: Text, out: JsonWriter): _root_.scala.Unit =
      x.value match {
        case str: Str =>
          out.writeObjectStart()
          out.writeNonEscapedAsciiKey("text")
          out.writeVal(str.s)
          out.writeObjectEnd()
        case Js(j) => j match {
          case Left(jsonObject) => JsonOrString.jsonObjectCodec.encodeValue(jsonObject, out)
          case Right(listOfJson) => JsonOrString.listOfJsonCodec.encodeValue(listOfJson, out)
        }
      }
  }
}
