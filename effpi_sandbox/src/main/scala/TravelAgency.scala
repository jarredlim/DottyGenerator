// Effpi - verified message-passing programs in Dotty
// Copyright 2020 Alceste Scalas
// Released under the MIT License: https://opensource.org/licenses/MIT

package effpi_sandbox.TravelAgency

import effpi.process.dsl._
import effpi.channel.Channel

import effpi_sandbox.S.types._
import effpi_sandbox.A.types._
import effpi_sandbox.B.types._

import effpi_sandbox.S.implementation._
import effpi_sandbox.A.implementation._
import effpi_sandbox.B.implementation._

import effpi_sandbox.caseclass._

// To run this example, try:
// sbt 'tests/runMain effpi_sandbox.TravelAgency.Main'
object Main {
  def main(): Unit = main(Array())

  def main(args: Array[String]) = {
    println("Successfully compiled! Running now...")
    implicit val ps = effpi.system.ProcessSystemRunnerImproved()

    val(c1, c2, c3, c4, c5, c6) = 
     (Channel[Query](),
     Channel[Full|Available](),
     Channel[Reject|Confirm](),
     Channel[Suggest](),
     Channel[Full|Quote](),
     Channel[OK|No]()) 

    eval(par(
     s(c1, c2, c3),
     a(c4, c1, c2, c5, c6, c3, c3, c5),
     b(c4, c5, c6)))


    Thread.sleep(1000)
    ps.kill()
  }
}