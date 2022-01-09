package com.github.plokhotnyuk.jsoniter_scala.macros

object Levels {

  enum InnerLevel(name: String, ordinal: Int) extends Enum[InnerLevel] {
    case HIGH extends InnerLevel("HIGH", 0)
    case LOW extends InnerLevel("LOW", 1)
  }

}