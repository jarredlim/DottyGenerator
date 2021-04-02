// Effpi - verified message-passing programs in Dotty
// Copyright 2020 Alceste Scalas
// Released under the MIT License: https://opensource.org/licenses/MIT

// This is the two-buyer protocol example from session types literature,
// implemented using match types
package effpi_sandbox.test

import scala.concurrent.duration.Duration

import effpi.recurse._
import effpi.process._
import effpi.process.dsl._
import effpi.channel.{Channel, InChannel, OutChannel}
import java.util.Date

package types {

case class QUIT()
case class RES(x1:Int)
case class ADD(x1:Int,x2:Int)


type Client[
C_Client_Svr_1 <: OutChannel[ADD|QUIT],
C_Client_Svr_2 <: InChannel[RES]] =
 Rec[RecClient1, ((Out[C_Client_Svr_1,ADD] >>: In[C_Client_Svr_2, RES, (x:RES) => Loop[RecClient1]])|(Out[C_Client_Svr_1,QUIT] >>: PNil))]

type Svr2[
X2 <: ADD|QUIT,
C_Svr_Client_2 <: OutChannel[RES]] <: Process =
 X2 match {
case ADD => Out[C_Svr_Client_2,RES] >>: Loop[RecSvr1]
case QUIT => PNil
}

type Svr[
C_Svr_Client_1 <: InChannel[ADD|QUIT],
C_Svr_Client_2 <: OutChannel[RES]] =
 Rec[RecSvr1, In[C_Svr_Client_1, ADD|QUIT, (x:ADD|QUIT) => Svr2[x.type,C_Svr_Client_2]]]


}

package implementation {
  import types._
  implicit val timeout: Duration = Duration("30 seconds")
  import effpi.process.dsl._
  var i = 0
  var acc = 0

     def client(
      c_Client_Svr_1: OutChannel[ADD|QUIT],
      c_Client_Svr_2: InChannel[RES]
   ):Client[c_Client_Svr_1.type,c_Client_Svr_2.type] ={
      rec(RecClient1){
         print("Client:entering loop RecClient1\n")
         print("Client:Making selection through channel c_Client_Svr_1\n")
         if(i < 10){
            print("Client:Sending ADD through channel c_Client_Svr_1\n")
            send(c_Client_Svr_1,ADD(i,acc)) >> {
               i += 1
               receive(c_Client_Svr_2) {
                  (x:RES) =>
                  acc = x.x1
                  print("Client:Receive type RES through channel c_Client_Svr_2\n")
                  print("Client:go to loop RecClient1\n")
                  loop(RecClient1)
               }
            }
         }
         else{
            print("Client:Sending QUIT through channel c_Client_Svr_1\n")
            send(c_Client_Svr_1,QUIT()) >> {
               print("Client:Terminating...\n")
               print(s"Final Value is ${acc}\n")
               nil
            }
         }
      }
   }


   def svr2(
      x2: ADD|QUIT,
      c_Svr_Client_2: OutChannel[RES]
   ):Svr2[x2.type,c_Svr_Client_2.type] =
      x2 match {
         case x2 : ADD => {
            print("Svr:Received type ADD from x2\n")
            print("Svr:Sending RES through channel c_Svr_Client_2\n")
            send(c_Svr_Client_2,RES(x2.x1 + x2.x2)) >> {
               print("Svr:go to loop RecSvr1\n")
               loop(RecSvr1)
            }
         }
         case x2 : QUIT => {
            print("Svr:Received type QUIT from x2\n")
            print("Svr:Terminating...\n")
            nil
         }
      }


   def svr(
      c_Svr_Client_1: InChannel[ADD|QUIT],
      c_Svr_Client_2: OutChannel[RES]
   ):Svr[c_Svr_Client_1.type,c_Svr_Client_2.type] ={
      rec(RecSvr1){
         print("Svr:entering loop RecSvr1\n")
         receive(c_Svr_Client_1) {
            (x:ADD|QUIT) =>
            print("Svr:Receive type ADD|QUIT through channel c_Svr_Client_1\n")
            svr2(x,c_Svr_Client_2)
         }
      }
   }



}

// To run this example, try:
// sbt "examples/runMain effpi.examples.demo.twobuyer.Main"
object Main {
  import types._
  import implementation._

  def main(): Unit = main(Array())

  def main(args: Array[String]) = {
    println("Successfully compiled! Running now...")
    implicit val ps = effpi.system.ProcessSystemRunnerImproved()

    val(c1, c2) = (Channel[ADD|QUIT](), Channel[RES]())
eval(par(client(c1, c2), svr(c1, c2)))


    Thread.sleep(1000)
    ps.kill()
  }
}
