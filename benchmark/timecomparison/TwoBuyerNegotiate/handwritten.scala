// Effpi - verified message-passing programs in Dotty
// Copyright 2020 Alceste Scalas
// Released under the MIT License: https://opensource.org/licenses/MIT

// This is the two-buyer protocol example from session types literature,
// implemented using match types
package effpi.examples.demo.twobuyernegotiate

import scala.concurrent.duration.Duration

import effpi.process._
import effpi.process.dsl._
import effpi.channel.{Channel, InChannel, OutChannel}
import java.time.LocalDate

package types {
  import effpi.process.dsl._

  // Message classes
  case class START(location: String)
  case class PRICE(amount: Int)
  case class OFFER(amount: Int)
  case class BUY(address: String)
  case class CONFIRM(departure: LocalDate)
  case class CANCEL()
  case class NEGOTIATE()

  type A[CStart <: OutChannel[START],
         CPrice <: InChannel[PRICE],
         COffer <: InChannel[OFFER],
         CBuy <: OutChannel[BUY|CANCEL| NEGOTIATE],
         CConfirm <: InChannel[CONFIRM]] =
          Out[CStart, START] >>: In[CPrice, PRICE, PRICE => Rec[RecX, In[COffer, OFFER, OFFER =>
           (Out[CBuy, BUY] >>: In[CConfirm, CONFIRM, CONFIRM => PNil])|(Out[CBuy, CANCEL] >>: PNil)|(Out[CBuy, NEGOTIATE] >>: Loop[RecX])]]]

  type B[CPrice <: InChannel[PRICE],
         COffer <: OutChannel[OFFER],
         CBMatch <: InChannel[CONFIRM| CANCEL| NEGOTIATE]] =
          In[CPrice, PRICE, PRICE => Rec[RecY, Out[COffer, OFFER] >>: In[CBMatch, CONFIRM|CANCEL|NEGOTIATE, (x:CONFIRM|CANCEL| NEGOTIATE) => B2[x.type]]]]


  type B2[X <: CONFIRM| CANCEL| NEGOTIATE] <: Process =
      X match {
        case CONFIRM => PNil
        case CANCEL => PNil
        case NEGOTIATE => Loop[RecY]
      }

  type S[CStart <: InChannel[START],
         CPrice1 <: OutChannel[PRICE],
         CPrice2 <: OutChannel[PRICE],
         COffer1 <: InChannel[OFFER],
        COffer2 <: OutChannel[OFFER],
        CSMatch <: InChannel[BUY| CANCEL| NEGOTIATE],
        CConfirm1 <: OutChannel[CONFIRM],
        CSMatch2 <: OutChannel[BUY| CANCEL| NEGOTIATE]] =
          In[CStart, START, START => Out[CPrice1, PRICE] >>: Out[CPrice2, PRICE] >>: Rec[RecZ, In[COffer1, OFFER, OFFER => Out[COffer2, OFFER] >>: In[CSMatch, (BUY| CANCEL| NEGOTIATE), (x:BUY| CANCEL| NEGOTIATE) => S2[x.type,CConfirm1, CSMatch2]]]]]

  type S2[
    X <: BUY| CANCEL| NEGOTIATE,
    CConfirm1 <: OutChannel[CONFIRM],
  CSMatch2 <: OutChannel[BUY| CANCEL| NEGOTIATE]] <: Process
  = X match {
    case CONFIRM => Out[CConfirm1, CONFIRM] >>: Out[CSMatch2, CONFIRM] >>: PNil
    case CANCEL => Out[CSMatch2, CANCEL] >>: PNil
    case NEGOTIATE => Out[CSMatch2, NEGOTIATE] >>: Loop[RecZ]
  }

}

package implementation {
  import types._
  implicit val timeout: Duration = Duration("30 seconds")
  import effpi.process.dsl._
  var iteration = 0

  def a(cStart : OutChannel[START],
  cPrice : InChannel[PRICE],
  cOffer : InChannel[OFFER],
  cDecision : OutChannel[BUY|CANCEL| NEGOTIATE],
  cConfirm : InChannel[CONFIRM]): A[cStart.type, cPrice.type, cOffer.type, cDecision.type, cConfirm.type] = {
    send(cStart, START("Las Vegas")) >> {
      receive(cPrice){
        (x1: PRICE) =>
          rec(RecX){
          receive(cOffer){
            (x2: OFFER) =>
              if(x2.amount >= x1.amount /2 ){
                println("Buy")
                  send(cDecision, BUY("Baker Street")) >> {
                    receive(cConfirm){(x3: CONFIRM) => nil}
                  }
              }else if(iteration > 10){
                println("Cancel")
                   send(cDecision, CANCEL()) >> {nil}
              }else{
                println("Negotiating")
                 iteration += 1
                 send(cDecision, NEGOTIATE()) >> {loop(RecX)}
              }
        }
        }
      }
    }
  }

  def b(cPrice : InChannel[PRICE],
        cOffer : OutChannel[OFFER],
        cBMatch : InChannel[CONFIRM| CANCEL| NEGOTIATE],
        ): B[cPrice.type, cOffer.type, cBMatch.type] =
          {
            receive(cPrice){
              (x1: PRICE) =>
                rec(RecY){
                   send(cOffer, OFFER(100 + iteration* 10)) >> {
                      receive(cBMatch){
                        (x2: CONFIRM| CANCEL| NEGOTIATE) => b2(x2)
                      }
                   }
                }
            }
          }

    def b2(x2: CONFIRM| CANCEL| NEGOTIATE): B2[x2.type] = x2 match {
        case _: CONFIRM => nil
        case _:CANCEL => nil
        case _: NEGOTIATE => loop(RecY)
    }

    def s(cStart : InChannel[START],
    cPrice1 : OutChannel[PRICE],
    cPrice2 : OutChannel[PRICE],
    cOffer1 : InChannel[OFFER],
   cOffer2 : OutChannel[OFFER],
   cSMatch1 : InChannel[BUY| CANCEL| NEGOTIATE],
   cSMatch2 : OutChannel[CONFIRM| CANCEL| NEGOTIATE],
   cConfirm1 : OutChannel[CONFIRM]) = {
     receive(cStart){
       (x1: START) =>
          send(cPrice1, PRICE(300)) >> send(cPrice2, PRICE(2)) >> {
           rec(RecZ){
          receive(cOffer1){
            (x2: OFFER) =>
              send(cOffer2, OFFER(x2.amount)) >> {
                receive(cSMatch1){
                        (x3: BUY| CANCEL| NEGOTIATE) => s2(x3, cConfirm1, cSMatch2)
                      }
              }
          }
          }
     }
     }
   }

   def s2(x : BUY| CANCEL| NEGOTIATE,
   cConfirm1 : OutChannel[CONFIRM],
   cSMatch2 : OutChannel[CONFIRM| CANCEL| NEGOTIATE]) = x match{
     case _: BUY => send(cConfirm1, CONFIRM(LocalDate.now().plusWeeks(1))) >> send(cSMatch2, CONFIRM(LocalDate.now().plusWeeks(1))) >> nil
     case _: CANCEL => send(cSMatch2, CANCEL()) >> nil
     case _: NEGOTIATE => send(cSMatch2, NEGOTIATE()) >> loop(RecZ)
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

    val(cStart, cPrice1, cPrice2, cOffer1, cOffer2, cMatch, cSMatch2, cConfirm1) = (Channel[START](),Channel[PRICE](), Channel[PRICE](), Channel[OFFER](), Channel[OFFER](), Channel[BUY| CANCEL| NEGOTIATE](), Channel[CONFIRM | CANCEL | NEGOTIATE](), Channel[CONFIRM]())
    eval(par(a(cStart,cPrice1,cOffer2, cMatch, cConfirm1), b(cPrice2, cOffer1, cSMatch2), s(cStart, cPrice1, cPrice2, cOffer1, cOffer2, cMatch,cSMatch2, cConfirm1)))

    Thread.sleep(5000) // Wait 5 seconds
    ps.kill()
  }
}
