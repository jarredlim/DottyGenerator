// Effpi - verified message-passing programs in Dotty
// Copyright 2020 Alceste Scalas
// Released under the MIT License: https://opensource.org/licenses/MIT

package effpi_sandbox.Broadcast

import effpi.process.dsl._
import effpi.channel.Channel

import effpi_sandbox.r.types._
import effpi_sandbox.q.types._
import effpi_sandbox.p.types._

import effpi_sandbox.r.implementation._
import effpi_sandbox.q.implementation._
import effpi_sandbox.p.implementation._

import effpi_sandbox.caseclass._

// To run this example, try:
// sbt 'tests/runMain effpi_sandbox.Broadcast.Main'
object Main {
  def main(): Unit = main(Array())

  def main(args: Array[String]) = {
    println("Successfully compiled! Running now...")
    implicit val ps = effpi.system.ProcessSystemRunnerImproved()

    val(c1, c2, c3, c4) = 
     (Channel.async[Data](),
     Channel.async[Data](),
     Channel.async[req|Ok](),
     Channel.async[Data|Ko]()) 

    eval(par(
     r(c2, c3, c3, c4),
     q(c1, c3, c4),
     p(c1, c2)))


    Thread.sleep(1000)
    ps.kill()
  }
}