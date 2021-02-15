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
import java.time.LocalDate

package types {

case class split(x1:Int)
case class title(x1:String)
case class cancel()
case class quote(x1:Int)
case class buy()
case class reject()
case class accept()


type BuyerA2[ 
X2 <: accept|reject,
C_BuyerA_Seller_3 <: OutChannel[buy],
C_BuyerA_Seller_4 <: OutChannel[cancel]] <: Process = 
 X2 match { 
case accept => Out[C_BuyerA_Seller_3,buy] >>: PNil 
case reject => Out[C_BuyerA_Seller_4,cancel] >>: PNil 
} 

type BuyerA[ 
C_BuyerA_Seller_1 <: OutChannel[title],
C_BuyerA_Seller_2 <: InChannel[quote],
C_BuyerA_BuyerB_1 <: OutChannel[split],
C_BuyerA_BuyerB_2 <: InChannel[accept|reject],
C_BuyerA_Seller_3 <: OutChannel[buy],
C_BuyerA_Seller_4 <: OutChannel[cancel]] = 
 Out[C_BuyerA_Seller_1,title] >>: In[C_BuyerA_Seller_2, quote, quote => 
Out[C_BuyerA_BuyerB_1,split] >>: In[C_BuyerA_BuyerB_2, accept|reject, (x:accept|reject) => BuyerA2[x.type,C_BuyerA_Seller_3,C_BuyerA_Seller_4]]] 

type Seller2[ 
X2 <: cancel|buy] <: Process = 
 X2 match { 
case cancel => PNil 
case buy => PNil 
} 

type Seller[ 
C_BuyerA_Seller_1 <: InChannel[title],
C_BuyerA_Seller_2 <: OutChannel[quote],
C_BuyerB_Seller_1 <: OutChannel[quote],
C_BuyerA_Seller_3 <: InChannel[cancel|buy]] = 
 In[C_BuyerA_Seller_1, title, title => 
Out[C_BuyerA_Seller_2,quote] >>: Out[C_BuyerB_Seller_1,quote] >>: In[C_BuyerA_Seller_3, cancel|buy, (x:cancel|buy) => Seller2[x.type]]] 

type BuyerB[ 
C_BuyerB_Seller_1 <: InChannel[quote],
C_BuyerA_BuyerB_1 <: InChannel[split],
C_BuyerA_BuyerB_2 <: OutChannel[accept|reject]] = 
 In[C_BuyerB_Seller_1, quote, quote => 
In[C_BuyerA_BuyerB_1, split, split => 
(Out[C_BuyerA_BuyerB_2,accept] >>: PNil)|(Out[C_BuyerA_BuyerB_2,reject] >>: PNil)]] 


}

package implementation {
  import types._
  implicit val timeout: Duration = Duration("30 seconds")
  import effpi.process.dsl._

  def buyerA2( 
x2: accept|reject,
c_BuyerA_Seller_3: OutChannel[buy],
c_BuyerA_Seller_4: OutChannel[cancel]):BuyerA2[x2.type,c_BuyerA_Seller_3.type,c_BuyerA_Seller_4.type] =  x2 match { 
case x2 : accept => { 
 print("BuyerA: Received type accept from x2\n")
print("BuyerA:Sending buy through channel c_BuyerA_Seller_3\n") 
 send(c_BuyerA_Seller_3,buy()) >> {
 print("BuyerA: Terminating....\n") 
 nil } 
 } 
case x2 : reject => { 
 print("BuyerA: Received type reject from x2\n")
print("BuyerA:Sending cancel through channel c_BuyerA_Seller_4\n") 
 send(c_BuyerA_Seller_4,cancel()) >> {
 print("BuyerA: Terminating....\n") 
 nil } 
 } 
} 

def buyerA( 
c_BuyerA_Seller_1: OutChannel[title],
c_BuyerA_Seller_2: InChannel[quote],
c_BuyerA_BuyerB_1: OutChannel[split],
c_BuyerA_BuyerB_2: InChannel[accept|reject],
c_BuyerA_Seller_3: OutChannel[buy],
c_BuyerA_Seller_4: OutChannel[cancel]):BuyerA[c_BuyerA_Seller_1.type,c_BuyerA_Seller_2.type,c_BuyerA_BuyerB_1.type,c_BuyerA_BuyerB_2.type,c_BuyerA_Seller_3.type,c_BuyerA_Seller_4.type] ={ 
 print("BuyerA:Sending title through channel c_BuyerA_Seller_1\n") 
 send(c_BuyerA_Seller_1,title("wucucqlxqw")) >> {
 receive(c_BuyerA_Seller_2) { 
 (x:quote) => 
 print("BuyerA: Receive type quote through channel c_BuyerA_Seller_2\n") 
 print("BuyerA:Sending split through channel c_BuyerA_BuyerB_1\n") 
 send(c_BuyerA_BuyerB_1,split(53)) >> {
 receive(c_BuyerA_BuyerB_2) { 
 (x:accept|reject) => 
 print("BuyerA: Receive type accept|reject through channel c_BuyerA_BuyerB_2\n") 
 buyerA2(x,c_BuyerA_Seller_3,c_BuyerA_Seller_4) 
} } 
} } 
} 

def seller2( 
x2: cancel|buy):Seller2[x2.type] =  x2 match { 
case x2 : cancel => { 
 print("Seller: Received type cancel from x2\n")
print("Seller: Terminating....\n") 
 nil 
 } 
case x2 : buy => { 
 print("Seller: Received type buy from x2\n")
print("Seller: Terminating....\n") 
 nil 
 } 
} 

def seller( 
c_BuyerA_Seller_1: InChannel[title],
c_BuyerA_Seller_2: OutChannel[quote],
c_BuyerB_Seller_1: OutChannel[quote],
c_BuyerA_Seller_3: InChannel[cancel|buy]):Seller[c_BuyerA_Seller_1.type,c_BuyerA_Seller_2.type,c_BuyerB_Seller_1.type,c_BuyerA_Seller_3.type] ={ 
 receive(c_BuyerA_Seller_1) { 
 (x:title) => 
 print("Seller: Receive type title through channel c_BuyerA_Seller_1\n") 
 print("Seller:Sending quote through channel c_BuyerA_Seller_2\n") 
 send(c_BuyerA_Seller_2,quote(25)) >> {
 print("Seller:Sending quote through channel c_BuyerB_Seller_1\n") 
 send(c_BuyerB_Seller_1,quote(89)) >> {
 receive(c_BuyerA_Seller_3) { 
 (x:cancel|buy) => 
 print("Seller: Receive type cancel|buy through channel c_BuyerA_Seller_3\n") 
 seller2(x) 
} } } 
} 
} 

def buyerB( 
c_BuyerB_Seller_1: InChannel[quote],
c_BuyerA_BuyerB_1: InChannel[split],
c_BuyerA_BuyerB_2: OutChannel[accept|reject]):BuyerB[c_BuyerB_Seller_1.type,c_BuyerA_BuyerB_1.type,c_BuyerA_BuyerB_2.type] ={ 
 receive(c_BuyerB_Seller_1) { 
 (x:quote) => 
 print("BuyerB: Receive type quote through channel c_BuyerB_Seller_1\n") 
 receive(c_BuyerA_BuyerB_1) { 
 (x:split) => 
 print("BuyerB: Receive type split through channel c_BuyerA_BuyerB_1\n") 
 val r = scala.util.Random
 val decision = r.nextInt(2)
print("BuyerB:Making selection through channel c_BuyerA_BuyerB_2\n")
if(decision == 0){
print("BuyerB:Sending accept through channel c_BuyerA_BuyerB_2\n") 
 send(c_BuyerA_BuyerB_2,accept()) >> {
 print("BuyerB: Terminating....\n") 
 nil } 
 }
else{
print("BuyerB:Sending reject through channel c_BuyerA_BuyerB_2\n") 
 send(c_BuyerA_BuyerB_2,reject()) >> {
 print("BuyerB: Terminating....\n") 
 nil } 
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

    val(c_BuyerA_Seller_1, c_BuyerA_Seller_2, c_BuyerA_BuyerB_1, c_BuyerA_BuyerB_2, c_BuyerA_Seller_3, c_BuyerB_Seller_1) = (Channel[title](), Channel[quote](), Channel[split](), Channel[accept|reject](),Channel[buy|cancel](), Channel[quote]())
    eval(par(buyerA(c_BuyerA_Seller_1, c_BuyerA_Seller_2, c_BuyerA_BuyerB_1, c_BuyerA_BuyerB_2, c_BuyerA_Seller_3, c_BuyerA_Seller_3), buyerB(c_BuyerB_Seller_1, c_BuyerA_BuyerB_1, c_BuyerA_BuyerB_2), seller(c_BuyerA_Seller_1, c_BuyerA_Seller_2, c_BuyerB_Seller_1, c_BuyerA_Seller_3)))

    Thread.sleep(1000)
    ps.kill()
  }
}
