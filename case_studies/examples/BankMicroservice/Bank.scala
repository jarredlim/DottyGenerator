// Effpi - verified message-passing programs in Dotty
// Copyright 2020 Alceste Scalas
// Released under the MIT License: https://opensource.org/licenses/MIT

package sandbox.Bank

import effpi.process.dsl._
import effpi.channel.Channel

import sandbox.Client.types._
import sandbox.SvrVer.types._
import sandbox.SvrAct.types._
import sandbox.Svr.types._

import sandbox.Client.implementation._
import sandbox.SvrVer.implementation._
import sandbox.SvrAct.implementation._
import sandbox.Svr.implementation._

import sandbox.caseclass._

// To run this example, try:
// sbt 'case_studies/runMain sandbox.Bank.Main'
object Main {
  def main(): Unit = main(Array())

  def main(args: Array[String]) = {
    println("Successfully compiled! Running now...")
    implicit val ps = effpi.system.ProcessSystemRunnerImproved()

    val(c1, c2, c3, c4, c5, c6, c7, c8, c9, c10, c11, c12) = 
     (Channel[Connect](),
     Channel[Login](),
     Channel[Retry|Cancel|Success](),
     Channel[Withdraw1|Deposit1|Cancel](),
     Channel[Login](),
     Channel[Success|Fail](),
     Channel[Cancel|Continue](),
     Channel[Retry|Cancel](),
     Channel[Retry|Cancel|Continue](),
     Channel[Withdraw2|Cancel|Deposit2](),
     Channel[Success|Fail](),
     Channel[Retry|Cancel]()) 

    eval(par(
     client(c1, c2, c4, c3),
     svrVer(c5, c6, c7, c8),
     svrAct(c9, c10, c11, c12),
     svr(c1, c2, c5, c6, c9, c4, c10, c11, c3, c7, c3, c7, c12, c12, c10, c10, c8, c9, c8, c9)))


    Thread.sleep(1000)
    ps.kill()
  }
}