package com.github.plokhotnyuk.jsoniter_scala.examples

import com.github.plokhotnyuk.jsoniter_scala.macros._
import com.github.plokhotnyuk.jsoniter_scala.core._

import zio.console._

/**
  * Example of basic usage from README.md
  */
object Example01 extends zio.App {
  case class Device(id: Int, model: String)

  case class User(name: String, devices: Seq[Device])

  implicit val codec: JsonValueCodec[User] = JsonCodecMaker.make

  def run(args: List[String]) = myAppLogic.exitCode

  val myAppLogic = {
    val user = readFromArray("""{"name":"John","devices":[{"id":1,"model":"HTC One X"}]}""".getBytes("UTF-8"))
    val json = writeToArray(User(name = "John", devices = Seq(Device(id = 2, model = "iPhone X"))))

    for {
      _    <- putStrLn(user.toString)
      _    <- putStrLn(new String(json, "UTF-8"))
    } yield ()
  }
}
