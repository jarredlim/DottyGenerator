package effpi_sandbox.A.implementation

import effpi_sandbox.caseclass._
import effpi_sandbox.A.types._
import effpi.process._
import effpi.process.dsl._
import effpi.channel.{InChannel, OutChannel}
import scala.concurrent.duration.Duration
import effpi.recurse._
import java.util.Date

implicit val timeout: Duration = Duration("60 seconds")

   private def a_3(
      x_3: OK|No,
      c_A_S_3: OutChannel[Confirm],
      c_A_S_4: OutChannel[Reject]
   ):A_3[x_3.type,c_A_S_3.type,c_A_S_4.type] =
      x_3 match {
         case x_3 : OK => {
            print("A:Actual type Received from x_3: OK\n")
            print("A:Sending Confirm through channel c_A_S_3\n")
            send(c_A_S_3,Confirm(-1)) >> {
               print("A:Terminating...\n")
               nil
            }
         }
         case x_3 : No => {
            print("A:Actual type Received from x_3: No\n")
            print("A:Sending Reject through channel c_A_S_4\n")
            send(c_A_S_4,Reject()) >> {
               print("A:Terminating...\n")
               nil
            }
         }
      }


   private def a_2(
      x_2: Available|Full,
      c_A_B_2: OutChannel[Quote],
      c_A_B_4: InChannel[OK|No],
      c_A_S_3: OutChannel[Confirm],
      c_A_S_4: OutChannel[Reject],
      c_A_B_3: OutChannel[Full]
   ):A_2[x_2.type,c_A_B_2.type,c_A_B_4.type,c_A_S_3.type,c_A_S_4.type,c_A_B_3.type] =
      x_2 match {
         case x_2 : Available => {
            print("A:Actual type Received from x_2: Available\n")
            print("A:Sending Quote through channel c_A_B_2\n")
            send(c_A_B_2,Quote(-1)) >> {
               receive(c_A_B_4) {
                  (x_3:OK|No) =>
                  print("A:Receive type OK|No through channel c_A_B_4\n")
                  a_3(x_3,c_A_S_3,c_A_S_4)
               }
            }
         }
         case x_2 : Full => {
            print("A:Actual type Received from x_2: Full\n")
            print("A:Sending Full through channel c_A_B_3\n")
            send(c_A_B_3,Full()) >> {
               print("A:go to loop RecA_0\n")
               loop(RecA_0)
            }
         }
      }


   def a(
      c_A_B_1: InChannel[Suggest],
      c_A_S_1: OutChannel[Query],
      c_A_S_2: InChannel[Available|Full],
      c_A_B_2: OutChannel[Quote],
      c_A_B_4: InChannel[OK|No],
      c_A_S_3: OutChannel[Confirm],
      c_A_S_4: OutChannel[Reject],
      c_A_B_3: OutChannel[Full]
   ):A[c_A_B_1.type,c_A_S_1.type,c_A_S_2.type,c_A_B_2.type,c_A_B_4.type,c_A_S_3.type,c_A_S_4.type,c_A_B_3.type] ={
      rec(RecA_0){
         print("A:entering loop RecA_0\n")
         receive(c_A_B_1) {
            (x:Suggest) =>
            print("A:Receive type Suggest through channel c_A_B_1\n")
            print("A:Sending Query through channel c_A_S_1\n")
            send(c_A_S_1,Query("REPLACE_ME")) >> {
               receive(c_A_S_2) {
                  (x_2:Available|Full) =>
                  print("A:Receive type Available|Full through channel c_A_S_2\n")
                  a_2(x_2,c_A_B_2,c_A_B_4,c_A_S_3,c_A_S_4,c_A_B_3)
               }
            }
         }
      }
   }



