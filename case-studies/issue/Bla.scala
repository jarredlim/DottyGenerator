// Effpi - verified message-passing programs in Dotty
// Copyright 2020 Alceste Scalas
// Released under the MIT License: https://opensource.org/licenses/MIT
package effpi.examples.demo.bla

import scala.concurrent.duration.Duration

import effpi.recurse._
import effpi.process._
import effpi.process.dsl._
import effpi.channel.{Channel, InChannel, OutChannel}
import java.util.Date

package types {

case class Test2()
case class BYE()
case class Test3()
case class ADD(w:Int)
case class HELLO(u:Int)
case class Test1()


type P[
C_P_S_1 <: OutChannel[Test1|Test2],
C_P_C_1 <: OutChannel[Test1|Test2],
C_P_C_2 <: OutChannel[Test2|Test3]] =
 ((Out[C_P_S_1,Test1] >>: ((Out[C_P_C_1,Test1] >>: PNil)|(Out[C_P_C_1,Test2] >>: PNil)))| (Out[C_P_S_1,Test2] >>: ((Out[C_P_C_2,Test2] >>: PNil)|(Out[C_P_C_2,Test3] >>: PNil))))




}

package implementation {
  import types._
  implicit val timeout: Duration = Duration("60 seconds")
  import effpi.process.dsl._

     def p(
      c_P_S_1: OutChannel[Test1|Test2],
      c_P_C_1: OutChannel[Test1|Test2],
      c_P_C_2: OutChannel[Test2|Test3]
   ):P[c_P_S_1.type,c_P_C_1.type,c_P_C_2.type] ={
      val decision = 1
      if(decision == 0){
         send(c_P_S_1,Test1()) >> {
            if(decision == 0){
               send(c_P_C_1,Test1()) >> {
                  nil
               }
            }
            else{
               send(c_P_C_1,Test2()) >> {
                  nil
               }
            }
         }
      }
      else{
         send(c_P_S_1,Test2()) >> {
            val decision = 0
            if(decision == 0){
               send(c_P_C_2,Test2()) >> {
                  nil
               }
            }
            else{
               send(c_P_C_2,Test3()) >> {
                  nil
               }
            }
         }
      }
   }



}

// To run this example, try:
// sbt "examples/runMain effpi.examples.demo.bla.Main"
object Main {
  import types._
  import implementation._

  def main(): Unit = main(Array())

  def main(args: Array[String]) = {
    println("Successfully compiled! Running now...")
    implicit val ps = effpi.system.ProcessSystemRunnerImproved()



    Thread.sleep(1000)
    ps.kill()
  }
}
