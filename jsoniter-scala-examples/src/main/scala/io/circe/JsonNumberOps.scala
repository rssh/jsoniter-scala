package io.circe

import com.github.plokhotnyuk.jsoniter_scala.core.JsonWriter

object JsonNumberOps {
  implicit class JsonNumberOps(number: JsonNumber) {
    def printNumber(out: JsonWriter) = number match {
      case JsonLong(value)       => out.writeVal(value)
      case JsonFloat(value)      => out.writeVal(value)
      case JsonDouble(value)     => out.writeVal(value)
      case JsonBigDecimal(value) => out.writeVal(value)
      //     case number: BiggerDecimalJsonNumber => out.writeVal(number.toDouble)
      case j: JsonDecimal => out.writeVal(j.toBiggerDecimal.toBigDecimal.get)
    }
  }
}
