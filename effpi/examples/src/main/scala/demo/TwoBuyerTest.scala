// Effpi - verified message-passing programs in Dotty
// Copyright 2020 Alceste Scalas
// Released under the MIT License: https://opensource.org/licenses/MIT

// This is the two-buyer protocol example from session types literature,
// implemented using match types
package effpi.examples.demo.twobuyertest

import scala.concurrent.duration.Duration

import effpi.process._
import effpi.process.dsl._
import effpi.channel.{Channel, InChannel, OutChannel}
import java.time.LocalDate

package types {

  case class empty5(x1:Int)
  case class empty4(x1:Int)
  case class empty1(x1:Int)
  case class empty3(x1:Int)
  case class quit()
  case class empty2(x1:Int)
  case class ok(x1:Int)
  
  type S2[ 
  X2 <: quit|ok,
  C_B_S_3 <: OutChannel[empty5],
  C_A_S_3 <: OutChannel[empty1|empty2]] <: Process = 
   X2 match { 
  case quit => PNil 
  case ok => Out[C_B_S_3,empty5] >>: (Out[C_A_S_3,empty1] >>: PNil)|(Out[C_A_S_3,empty2] >>: PNil) 
  } 
  
  type S[ 
  C_A_S_1 <: InChannel[empty1],
  C_A_S_2 <: OutChannel[empty2],
  C_B_S_1 <: OutChannel[empty3],
  C_B_S_2 <: InChannel[quit|ok],
  C_B_S_3 <: OutChannel[empty5],
  C_A_S_3 <: OutChannel[empty1|empty2]] = 
   In[C_A_S_1, empty1, empty1 => 
  Out[C_A_S_2,empty2] >>: Out[C_B_S_1,empty3] >>: In[C_B_S_2, quit|ok, (x:quit|ok) => S2[x.type,C_B_S_3,C_A_S_3]]] 
  
  type B2[ 
  X2 <: empty3|quit,
  C_A_B_5 <: OutChannel[empty4]] <: Process = 
   X2 match { 
  case empty3 => Out[C_A_B_5,empty4] >>: PNil 
  case quit => PNil 
  } 
  
  type B[ 
  C_B_S_1 <: InChannel[empty3],
  C_A_B_1 <: InChannel[empty4],
  C_A_B_2 <: OutChannel[ok|quit],
  C_B_S_2 <: OutChannel[ok],
  C_B_S_3 <: InChannel[empty5],
  C_A_B_3 <: OutChannel[empty2],
  C_A_B_4 <: InChannel[empty3|quit],
  C_A_B_5 <: OutChannel[empty4],
  C_B_S_4 <: OutChannel[quit]] = 
   In[C_B_S_1, empty3, empty3 => 
  In[C_A_B_1, empty4, empty4 => 
  (Out[C_A_B_2,ok] >>: Out[C_B_S_2,ok] >>: In[C_B_S_3, empty5, empty5 => 
  Out[C_A_B_3,empty2] >>: In[C_A_B_4, empty3|quit, (x:empty3|quit) => B2[x.type,C_A_B_5]]])|(Out[C_A_B_2,quit] >>: Out[C_B_S_4,quit] >>: PNil)]] 
  
  type A3[ 
  X3 <: empty1|empty2,
  C_A_B_3 <: InChannel[empty2],
  C_A_B_4 <: OutChannel[empty3|quit],
  C_A_B_5 <: InChannel[empty4]] <: Process = 
   X3 match { 
  case empty1 => In[C_A_B_3, empty2, empty2 => 
  (Out[C_A_B_4,empty3] >>: In[C_A_B_5, empty4, empty4 => 
  PNil])|(Out[C_A_B_4,quit] >>: PNil)] 
  case empty2 => PNil 
  } 
  
  type A2[ 
  X2 <: ok|quit,
  C_A_S_3 <: InChannel[empty1|empty2],
  C_A_B_3 <: InChannel[empty2],
  C_A_B_4 <: OutChannel[empty3|quit],
  C_A_B_5 <: InChannel[empty4]] <: Process = 
   X2 match { 
  case ok => In[C_A_S_3, empty1|empty2, (x:empty1|empty2) => A3[x.type,C_A_B_3,C_A_B_4,C_A_B_5]] 
  case quit => PNil 
  } 
  
  type A[ 
  C_A_S_1 <: OutChannel[empty1],
  C_A_S_2 <: InChannel[empty2],
  C_A_B_1 <: OutChannel[empty4],
  C_A_B_2 <: InChannel[ok|quit],
  C_A_S_3 <: InChannel[empty1|empty2],
  C_A_B_3 <: InChannel[empty2],
  C_A_B_4 <: OutChannel[empty3|quit],
  C_A_B_5 <: InChannel[empty4]] = 
   Out[C_A_S_1,empty1] >>: In[C_A_S_2, empty2, empty2 => 
  Out[C_A_B_1,empty4] >>: In[C_A_B_2, ok|quit, (x:ok|quit) => A2[x.type,C_A_S_3,C_A_B_3,C_A_B_4,C_A_B_5]]] 
  
  
  
  
  
  
}

package implementation {
  import types._
  implicit val timeout: Duration = Duration("30 seconds")
  import effpi.process.dsl._

}

// To run this example, try:
// sbt "examples/runMain effpi.examples.demo.twobuyer.Main"
object Main {
  import types._
  import implementation._

  def main(): Unit = main(Array())

  def main(args: Array[String]) = {
    println("Running demo...")
    implicit val ps = effpi.system.ProcessSystemRunnerImproved()
    ps.kill()
  }
}
