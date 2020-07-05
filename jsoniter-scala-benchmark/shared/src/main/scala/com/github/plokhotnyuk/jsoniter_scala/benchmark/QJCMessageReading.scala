package com.github.plokhotnyuk.jsoniter_scala.benchmark

import com.github.plokhotnyuk.jsoniter_scala.core._
import com.github.plokhotnyuk.jsoniter_scala.macros._
import org.openjdk.jmh.annotations.Benchmark

class QJCMessageReading extends QJCMessageBenchmark {
  private[this] implicit val codec: JsonValueCodec[QJCMessage] = JsonCodecMaker.make

  @Benchmark
  def jsoniterScala(): QJCMessage = readFromArray[QJCMessage](jsonBytes)
}