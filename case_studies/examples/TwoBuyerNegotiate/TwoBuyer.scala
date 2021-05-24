// Effpi - verified message-passing programs in Dotty
// Copyright 2020 Alceste Scalas
// Released under the MIT License: https://opensource.org/licenses/MIT

package sandbox.TwoBuyer

import effpi.process.dsl._
import effpi.channel.Channel

import sandbox.B.implementation._
import sandbox.S.implementation._
import sandbox.A.implementation._

import sandbox.caseclass._

// To run this example, try:
// sbt 'case_studies/runMain sandbox.Bank.Main'
object Main {
  def main(): Unit = main(Array())

  def main(args: Array[String]) = {
    println("Successfully compiled! Running now...")
    implicit val ps = effpi.system.ProcessSystemRunnerImproved()

    val(c1, c2, c3, c4, c5, c6, c7, c8) = (Channel[PRICE](), 
                                           Channel[OFFER](), 
                                           Channel[CONFIRM|CANCEL|NEGOTIATE](), 
                                           Channel[START](), 
                                           Channel[PRICE](), 
                                           Channel[OFFER](), 
                                           Channel[CANCEL|NEGOTIATE|BUY](), 
                                           Channel[CONFIRM]())
eval(par(b(c1, c2, c3), 
        s(c4, c5, c1, c2, c6, c7, c8, c3, c3, c3), 
        a(c4, c5, c6, c7, c8)))


    Thread.sleep(1000)
    ps.kill()
  }
}