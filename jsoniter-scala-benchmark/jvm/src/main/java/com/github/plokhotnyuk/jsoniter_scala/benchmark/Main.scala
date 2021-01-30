package com.github.plokhotnyuk.jsoniter_scala.benchmark

import org.openjdk.jmh.infra.Blackhole
import org.openjdk.jmh.runner.RunnerException

object Main {
  @throws[RunnerException]
  def main (args: Array[String] ): Unit = {
    val benchmark = new ADTReading
    run("ADTReading.avSystemGenCodec", () => benchmark.avSystemGenCodec())
    run("ADTReading.borer", () => benchmark.borer())
    run("ADTReading.circe", () => benchmark.circe())
    run("ADTReading.jacksonScala", () => benchmark.jacksonScala())
    run("ADTReading.jsoniterScala", () => benchmark.jsoniterScala())
    run("ADTReading.playJson", () => benchmark.playJson())
    run("ADTReading.sprayJson", () => benchmark.sprayJson())
    run("ADTReading.uPickle", () => benchmark.uPickle())
    run("ADTReading.weePickle", () => benchmark.weePickle())
  }

  private def run[A](name: String, bench: () => A): Unit = {
    val bh = new Blackhole("Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.")
    println(name)
    println("Warming:")
    var j = 5
    while (j > 0) {
      oneSecRun(bench, bh)
      j -= 1
    }
    println("Main:")
    j = 5
    while (j > 0) {
      oneSecRun(bench, bh)
      j -= 1
    }
  }

  private[this] def oneSecRun[A](bench: () => A, bh: Blackhole): Unit = {
    val t = System.nanoTime()
    var n = 0L
    while (System.nanoTime() - t < 1000000000) {
      var i = 1000
      n += i
      while (i > 0) {
        bh.consume(bench())
        i -= 1
      }
    }
    println(s"Throughput: ${(n * 1000000000) / (System.nanoTime() - t)} ops/s")
  }
}
