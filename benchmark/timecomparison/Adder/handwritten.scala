// Effpi - verified message-passing programs in Dotty
// Copyright 2020 Alceste Scalas
// Released under the MIT License: https://opensource.org/licenses/MIT

// This is the two-buyer protocol example from session types literature,
// implemented using match types
package effpi.examples.demo.adder

import scala.concurrent.duration.Duration

import effpi.process._
import effpi.process.dsl._
import effpi.channel.{Channel, InChannel, OutChannel}
import java.time.LocalDate

package types {
  import effpi.process.dsl._

  case class ADD(x1: Int, x2: Int)
  case class RES(res: Int)
  case class QUIT()

  type Client[CAddQuit <: OutChannel[ADD | QUIT],
              CRes <: InChannel[RES]] =
                Rec[RecX, (Out[CAddQuit, ADD] >>: In[CRes, RES, RES => Loop[RecX]]) | (Out[CAddQuit, QUIT] >>: PNil)]

  type Server[CAddQuit <: InChannel[ADD | QUIT],
  CRes <: OutChannel[RES]] =
    Rec[RecY, In[CAddQuit, ADD|QUIT, (x: ADD|QUIT) => Server2[x.type, CRes]]]

  type Server2[X <: ADD | QUIT,
  CRes <: OutChannel[RES]] <: Process =
      X match {
        case ADD => Out[CRes, RES] >>: Loop[RecY]
        case QUIT => PNil
      }

}

package implementation {
  import types._
  implicit val timeout: Duration = Duration("30 seconds")
  import effpi.process.dsl._

  def client(cAddQuit : OutChannel[ADD | QUIT],
  cRes : InChannel[RES]): Client[cAddQuit.type, cRes.type] = {
    var acc = 0
    var i = 1
    rec(RecX){
      if(i > 10){
        println(s"Final Value is ${acc}")
        send(cAddQuit, QUIT()) >> nil
      }else{
        send(cAddQuit, ADD(acc, i)) >> {
          i += 1
          receive(cRes){
            (x : RES) =>
              acc = x.res
              loop(RecX)
          }
        }
      }
    }
  }

  def server(cAddQuit : InChannel[ADD | QUIT],
  cRes : OutChannel[RES]) : Server[cAddQuit.type, cRes.type]= {
    rec(RecY){
    receive(cAddQuit){
      (x: ADD| QUIT) => server2(x, cRes)
    }
    }
  }

  def server2(x2: ADD| QUIT, cRes : OutChannel[RES]):Server2[x2.type, cRes.type] = x2 match {
    case x: ADD => send(cRes, RES(x.x1 + x.x2)) >> {loop(RecY)}
    case _: QUIT => nil
  }


}

// To run this example, try:
// sbt "examples/runMain effpi.examples.demo.twobuyernegotiate.Main"
object Main {
  import types._
  import implementation._

  def main(): Unit = main(Array())

  def main(args: Array[String]) = {
    println("Running demo...")
    println()
    implicit val ps = effpi.system.ProcessSystemRunnerImproved()

    val(cAddQuit, cRes) = (Channel[ADD | QUIT](), Channel[RES]())
    eval(par(server(cAddQuit, cRes), client(cAddQuit, cRes)))
    Thread.sleep(5000) // Wait 5 seconds
    ps.kill()
  }
}
