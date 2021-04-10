// Effpi - verified message-passing programs in Dotty
// Copyright 2020 Alceste Scalas
// Released under the MIT License: https://opensource.org/licenses/MIT

package effpi_sandbox.Dummy

import scala.concurrent.duration.Duration

import effpi.recurse._
import effpi.process._
import effpi.process.dsl._
import effpi.channel.{Channel, InChannel, OutChannel}
import java.util.Date

package types {

case class Test1()
case class Test3()
case class Test4()
case class Test2()

type Svr2[ 
X2 <: Test1|Test3,
C_Svr_Client_2 <: OutChannel[Test2],
C_Svr_Client_3 <: OutChannel[Test1|Test3],
C_Svr_Client_6 <: OutChannel[Test4],
C_Svr_Client_7 <: OutChannel[Test1|Test3]] <: Process =
 X2 match { 
case Test1 => Out[C_Svr_Client_2,Test2] >>: ((Out[C_Svr_Client_3,Test1] >>: PNil)|(Out[C_Svr_Client_3,Test3] >>: PNil))
case Test3 => Out[C_Svr_Client_6,Test4] >>: ((Out[C_Svr_Client_7,Test1] >>: PNil)|(Out[C_Svr_Client_7,Test3] >>: PNil))
}


}

package implementation {
  import types._
  implicit val timeout: Duration = Duration("60 seconds")
  import effpi.process.dsl._

   def svr2(
   x2: Test1|Test3,
   c_Svr_Client_2: OutChannel[Test2],
   c_Svr_Client_3: OutChannel[Test1|Test3],
   c_Svr_Client_6: OutChannel[Test4],
   c_Svr_Client_7: OutChannel[Test1|Test3]):Svr2[x2.type,c_Svr_Client_2.type,c_Svr_Client_3.type,c_Svr_Client_6.type,c_Svr_Client_7.type] =
   x2 match {
      case x2 : Test1 => {
         send(c_Svr_Client_2,Test2()) >> {
            val decision = 1
            if(decision == 0){
               send(c_Svr_Client_3,Test1()) >> {
                  nil
               }
            }
            else{
               send(c_Svr_Client_3,Test3()) >> {
                  nil
               }
            }
         }
      }
      case x2 : Test3 => {
         send(c_Svr_Client_6,Test4()) >> {
            val decision = 0
            if(decision == 0){
               send(c_Svr_Client_7,Test1()) >> {
                  nil
               }
            }
            else{
               send(c_Svr_Client_7,Test3()) >> {
                  nil
               }
            }
         }
      }
   }






}

// To run this example, try:
// sbt 'tests/runMain effpi_sandbox.Dummy.Main'
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
