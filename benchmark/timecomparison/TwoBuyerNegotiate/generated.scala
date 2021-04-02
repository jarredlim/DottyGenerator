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

case class OFFER(amount:Int)
case class CONFIRM(departure:Date)
case class PRICE(amount:Int)
case class NEGOTIATE()
case class BUY(address:String)
case class START(location:String)
case class CANCEL()


type B2[
X2 <: NEGOTIATE|CONFIRM|CANCEL] <: Process =
 X2 match {
case NEGOTIATE => Loop[RecB2]
case CONFIRM => PNil
case CANCEL => PNil
}

type B[
C_B_S_1 <: InChannel[PRICE],
C_B_S_2 <: OutChannel[OFFER],
C_B_S_3 <: InChannel[NEGOTIATE|CONFIRM|CANCEL]] =
 In[C_B_S_1, PRICE, (x:PRICE) => Rec[RecB2, Out[C_B_S_2,OFFER] >>: In[C_B_S_3, NEGOTIATE|CONFIRM|CANCEL, (x:NEGOTIATE|CONFIRM|CANCEL) => B2[x.type]]]]

type S2[
X2 <: BUY|CANCEL|NEGOTIATE,
C_S_A_5 <: OutChannel[CONFIRM],
C_S_B_3 <: OutChannel[CONFIRM],
C_S_B_4 <: OutChannel[CANCEL],
C_S_B_5 <: OutChannel[NEGOTIATE]] <: Process =
 X2 match {
case BUY => Out[C_S_A_5,CONFIRM] >>: Out[C_S_B_3,CONFIRM] >>: PNil
case CANCEL => Out[C_S_B_4,CANCEL] >>: PNil
case NEGOTIATE => Out[C_S_B_5,NEGOTIATE] >>: Loop[RecS4]
}

type S[
C_S_A_1 <: InChannel[START],
C_S_A_2 <: OutChannel[PRICE],
C_S_B_1 <: OutChannel[PRICE],
C_S_B_2 <: InChannel[OFFER],
C_S_A_3 <: OutChannel[OFFER],
C_S_A_4 <: InChannel[BUY|CANCEL|NEGOTIATE],
C_S_A_5 <: OutChannel[CONFIRM],
C_S_B_3 <: OutChannel[CONFIRM],
C_S_B_4 <: OutChannel[CANCEL],
C_S_B_5 <: OutChannel[NEGOTIATE]] =
 In[C_S_A_1, START, (x:START) => Out[C_S_A_2,PRICE] >>: Out[C_S_B_1,PRICE] >>: Rec[RecS4, In[C_S_B_2, OFFER, (x:OFFER) => Out[C_S_A_3,OFFER] >>: In[C_S_A_4, BUY|CANCEL|NEGOTIATE, (x:BUY|CANCEL|NEGOTIATE) => S2[x.type,C_S_A_5,C_S_B_3,C_S_B_4,C_S_B_5]]]]]

type A[
C_A_S_1 <: OutChannel[START],
C_A_S_2 <: InChannel[PRICE],
C_A_S_3 <: InChannel[OFFER],
C_A_S_4 <: OutChannel[NEGOTIATE|BUY|CANCEL],
C_A_S_5 <: InChannel[CONFIRM]] =
 Out[C_A_S_1,START] >>: In[C_A_S_2, PRICE, (x:PRICE) => Rec[RecA3, In[C_A_S_3, OFFER, (x:OFFER) => ((Out[C_A_S_4,NEGOTIATE] >>: Loop[RecA3])|(Out[C_A_S_4,BUY] >>: In[C_A_S_5, CONFIRM, (x:CONFIRM) => PNil])|(Out[C_A_S_4,CANCEL] >>: PNil))]]]


}

package implementation {
  import types._
  implicit val timeout: Duration = Duration("30 seconds")
  import effpi.process.dsl._
  var iteration = 0

     def b2(
      x2: NEGOTIATE|CONFIRM|CANCEL
   ):B2[x2.type] =
      x2 match {
         case x2 : NEGOTIATE => {
            print("B:Received type NEGOTIATE from x2\n")
            print("B:go to loop RecB2\n")
            loop(RecB2)
         }
         case x2 : CONFIRM => {
            print("B:Received type CONFIRM from x2\n")
            print("B:Terminating...\n")
            nil
         }
         case x2 : CANCEL => {
            print("B:Received type CANCEL from x2\n")
            print("B:Terminating...\n")
            nil
         }
      }


   def b(
      c_B_S_1: InChannel[PRICE],
      c_B_S_2: OutChannel[OFFER],
      c_B_S_3: InChannel[NEGOTIATE|CONFIRM|CANCEL]
   ):B[c_B_S_1.type,c_B_S_2.type,c_B_S_3.type] ={
      receive(c_B_S_1) {
         (x:PRICE) =>
         print("B:Receive type PRICE through channel c_B_S_1\n")
         rec(RecB2){
            print("B:entering loop RecB2\n")
            print("B:Sending OFFER through channel c_B_S_2\n")
            send(c_B_S_2,OFFER(2)) >> {
               receive(c_B_S_3) {
                  (x:NEGOTIATE|CONFIRM|CANCEL) =>
                  print("B:Receive type NEGOTIATE|CONFIRM|CANCEL through channel c_B_S_3\n")
                  b2(x)
               }
            }
         }
      }
   }


   def s2(
      x2: BUY|CANCEL|NEGOTIATE,
      c_S_A_5: OutChannel[CONFIRM],
      c_S_B_3: OutChannel[CONFIRM],
      c_S_B_4: OutChannel[CANCEL],
      c_S_B_5: OutChannel[NEGOTIATE]
   ):S2[x2.type,c_S_A_5.type,c_S_B_3.type,c_S_B_4.type,c_S_B_5.type] =
      x2 match {
         case x2 : BUY => {
            print("S:Received type BUY from x2\n")
            print("S:Sending CONFIRM through channel c_S_A_5\n")
            send(c_S_A_5,CONFIRM(new Date())) >> {
               print("S:Sending CONFIRM through channel c_S_B_3\n")
               send(c_S_B_3,CONFIRM(new Date())) >> {
                  print("S:Terminating...\n")
                  nil
               }
            }
         }
         case x2 : CANCEL => {
            print("S:Received type CANCEL from x2\n")
            print("S:Sending CANCEL through channel c_S_B_4\n")
            send(c_S_B_4,CANCEL()) >> {
               print("S:Terminating...\n")
               nil
            }
         }
         case x2 : NEGOTIATE => {
            print("S:Received type NEGOTIATE from x2\n")
            print("S:Sending NEGOTIATE through channel c_S_B_5\n")
            send(c_S_B_5,NEGOTIATE()) >> {
               print("S:go to loop RecS4\n")
               loop(RecS4)
            }
         }
      }


   def s(
      c_S_A_1: InChannel[START],
      c_S_A_2: OutChannel[PRICE],
      c_S_B_1: OutChannel[PRICE],
      c_S_B_2: InChannel[OFFER],
      c_S_A_3: OutChannel[OFFER],
      c_S_A_4: InChannel[BUY|CANCEL|NEGOTIATE],
      c_S_A_5: OutChannel[CONFIRM],
      c_S_B_3: OutChannel[CONFIRM],
      c_S_B_4: OutChannel[CANCEL],
      c_S_B_5: OutChannel[NEGOTIATE]
   ):S[c_S_A_1.type,c_S_A_2.type,c_S_B_1.type,c_S_B_2.type,c_S_A_3.type,c_S_A_4.type,c_S_A_5.type,c_S_B_3.type,c_S_B_4.type,c_S_B_5.type] ={
      receive(c_S_A_1) {
         (x:START) =>
         print("S:Receive type START through channel c_S_A_1\n")
         print("S:Sending PRICE through channel c_S_A_2\n")
         send(c_S_A_2,PRICE(300)) >> {
            print("S:Sending PRICE through channel c_S_B_1\n")
            send(c_S_B_1,PRICE(300)) >> {
               rec(RecS4){
                  print("S:entering loop RecS4\n")
                  receive(c_S_B_2) {
                     (x:OFFER) =>
                     print("S:Receive type OFFER through channel c_S_B_2\n")
                     print("S:Sending OFFER through channel c_S_A_3\n")
                     send(c_S_A_3,OFFER(100 + 10 * iteration)) >> {
                        receive(c_S_A_4) {
                           (x:BUY|CANCEL|NEGOTIATE) =>
                           print("S:Receive type BUY|CANCEL|NEGOTIATE through channel c_S_A_4\n")
                           s2(x,c_S_A_5,c_S_B_3,c_S_B_4,c_S_B_5)
                        }
                     }
                  }
               }
            }
         }
      }
   }


   def a(
      c_A_S_1: OutChannel[START],
      c_A_S_2: InChannel[PRICE],
      c_A_S_3: InChannel[OFFER],
      c_A_S_4: OutChannel[NEGOTIATE|BUY|CANCEL],
      c_A_S_5: InChannel[CONFIRM]
   ):A[c_A_S_1.type,c_A_S_2.type,c_A_S_3.type,c_A_S_4.type,c_A_S_5.type] ={
      print("A:Sending START through channel c_A_S_1\n")
      send(c_A_S_1,START("Las Vegas")) >> {
         receive(c_A_S_2) {
            (x1:PRICE) =>
            print("A:Receive type PRICE through channel c_A_S_2\n")
            rec(RecA3){
               print("A:entering loop RecA3\n")
               receive(c_A_S_3) {
                  (x2:OFFER) =>
                  print("A:Receive type OFFER through channel c_A_S_3\n")
                  print("A:Making selection through channel c_A_S_4\n")
                  if(x2.amount < x1.amount/2 && iteration < 10){
                     print("A:Sending NEGOTIATE through channel c_A_S_4\n")
                     iteration += 1
                     send(c_A_S_4,NEGOTIATE()) >> {
                        print("A:go to loop RecA3\n")
                        loop(RecA3)
                     }
                  }
                  else if(x2.amount >= x1.amount/2){

                     print("A:Sending BUY through channel c_A_S_4\n")
                     send(c_A_S_4,BUY("Baker Street")) >> {
                        receive(c_A_S_5) {
                           (x:CONFIRM) =>
                           print("A:Receive type CONFIRM through channel c_A_S_5\n")
                           print("A:Terminating...\n")
                           nil
                        }
                     }
                  }
                  else{
                     print("A:Sending CANCEL through channel c_A_S_4\n")
                     send(c_A_S_4,CANCEL()) >> {
                        print("A:Terminating...\n")
                        nil
                     }
                  }
               }
            }
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

    val(c1, c2, c3, c4, c5, c6, c7, c8) = (Channel[PRICE](), Channel[OFFER](), Channel[CONFIRM|CANCEL|NEGOTIATE](), Channel[START](), Channel[PRICE](), Channel[OFFER](), Channel[CANCEL|NEGOTIATE|BUY](), Channel[CONFIRM]())
eval(par(b(c1, c2, c3), s(c4, c5, c1, c2, c6, c7, c8, c3, c3, c3), a(c4, c5, c6, c7, c8)))


    Thread.sleep(1000)
    ps.kill()
  }
}
