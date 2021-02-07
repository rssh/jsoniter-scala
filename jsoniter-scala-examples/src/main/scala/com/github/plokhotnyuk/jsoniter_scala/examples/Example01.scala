package com.github.plokhotnyuk.jsoniter_scala.examples

import com.github.plokhotnyuk.jsoniter_scala.core.{JsonValueCodec, _}
import com.github.plokhotnyuk.jsoniter_scala.macros.JsonCodecMaker

/**
  * Example of basic usage from README.md
  */
object Example01 {
  case class ComputationSpeed(value: Double, unit: ComputationSpeedUnit)

  object ComputationSpeed {
    def symbolToUnit(x: String): Option[ComputationSpeedUnit] = x match {
      case "IPC" => Some(ComputationSpeedUnit("IPC"))
      case "CPI" => Some(ComputationSpeedUnit("CPI"))
      case _ => None
    }
  }

  case class ComputationSpeedUnit(symbol: String)

  object ComputationSpeedUnit {
    implicit val codec: JsonValueCodec[ComputationSpeedUnit] = new JsonValueCodec[ComputationSpeedUnit] {
      override def encodeValue(x: ComputationSpeedUnit, out: JsonWriter): Unit = out.writeVal(x.symbol)

      override def decodeValue(in: JsonReader, default: ComputationSpeedUnit): ComputationSpeedUnit = {
        val x = in.readString(null)
        ComputationSpeed.symbolToUnit(x).getOrElse(in.decodeError(s"Unsupported unit of measure: $x"))
      }

      override val nullValue: ComputationSpeedUnit = null
    }
  }

  def main(args: Array[String]): Unit = {
    implicit val codec: JsonValueCodec[ComputationSpeed] = JsonCodecMaker.make

    val data = readFromArray[ComputationSpeed]("""{"value":3.0,"unit":"IPC"}""".getBytes("UTF-8"))
    val json = writeToArray(ComputationSpeed(0.25, ComputationSpeedUnit("CPI")))

    println(data)
    println(new String(json, "UTF-8"))
  }
}
