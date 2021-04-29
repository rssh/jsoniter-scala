package com.test

import com.github.plokhotnyuk.jsoniter_scala.core.{JsonReader, JsonValueCodec, JsonWriter}
import com.github.plokhotnyuk.jsoniter_scala.macros.{CodecMakerConfig, JsonCodecMaker, named}
import io.circe.syntax.{EncoderOps, KeyOps}
import io.circe.{Encoder, JsonObject}

case class LogRecord(
                      @named("threadId") thread: Option[Either[Long, String]],
                    )
object LogRecord {
  implicit val eitherLongOrStringCodec: JsonValueCodec[Either[Long, String]] = new JsonValueCodec[Either[Long, String]] {
    override def decodeValue(in: JsonReader, default: Either[Long, String]): Either[Long, String] = {
      val t = in.nextToken()
      in.rollbackToken()
      if (t == '"') Right(in.readString(null))
      else Left(in.readLong())
    }

    override def encodeValue(x: Either[Long, String], out: JsonWriter): Unit =
      x match {
        case Right(s) => out.writeVal(s)
        case Left(l) => out.writeVal(l)
      }

    override def nullValue: Either[Long, String] = null
  }
  implicit val logRecordCodec: JsonValueCodec[LogRecord] = JsonCodecMaker.make(CodecMakerConfig.withTransientNone(false))
  implicit val encoder: Encoder.AsObject[LogRecord] =
    Encoder.AsObject.instance { l =>
      JsonObject(
        "threadId"   := l.thread.map(_.fold(_.asJson, _.asJson)),
      )
    }}