package com.github.plokhotnyuk.jsoniter_scala.benchmark

import java.nio.charset.StandardCharsets.UTF_8

abstract class QJCMessageBenchmark extends CommonParams {
  var obj: QJCMessage = QJCMessage("INFO", "This is a log message that is long enough to be representative of an actual message.", 1, "Test", "main", 1400000000000000000L, 1)
  var jsonString: String = """{"level":"INFO","message":"This is a log message that is long enough to be representative of an actual message.","msgType":1,"source":"Test","thread":"main","timestamp":1400000000000000000,"version":1}"""
  var jsonBytes: Array[Byte] = jsonString.getBytes(UTF_8)
  var preallocatedBuf: Array[Byte] = new Array(jsonBytes.length + 100/*to avoid possible out of bounds error*/)
}

case class QJCMessage(level: String, message: String, msgType: Byte, source: String, thread: String, timestamp: Long, version: Byte)