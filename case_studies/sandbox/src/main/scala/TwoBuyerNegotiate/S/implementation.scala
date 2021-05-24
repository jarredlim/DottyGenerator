package sandbox.S.implementation

import sandbox.caseclass._
import sandbox.S.types._
import effpi.process._
import effpi.process.dsl._
import effpi.channel.{InChannel, OutChannel}
import scala.concurrent.duration.Duration
import effpi.recurse._
import java.util.Date

implicit val timeout: Duration = Duration("30 seconds")

private def s_2(
   x_2: BUY|CANCEL|NEGOTIATE,
   c_S_A_5: OutChannel[CONFIRM],
   c_S_B_3: OutChannel[CONFIRM],
   c_S_B_4: OutChannel[CANCEL],
   c_S_B_5: OutChannel[NEGOTIATE]
):S_2[x_2.type,c_S_A_5.type,c_S_B_3.type,c_S_B_4.type,c_S_B_5.type] =
   x_2 match {
      case x_2 : BUY => {
         print("S:Received type BUY from x_2\n")
         print("S:Sending CONFIRM through channel c_S_A_5\n")
         send(c_S_A_5,CONFIRM(new Date())) >> {
            print("S:Sending CONFIRM through channel c_S_B_3\n")
            send(c_S_B_3,CONFIRM(new Date())) >> {
               print("S:Terminating...\n")
               nil
            }
         }
      }
      case x_2 : CANCEL => {
         print("S:Received type CANCEL from x_2\n")
         print("S:Sending CANCEL through channel c_S_B_4\n")
         send(c_S_B_4,CANCEL()) >> {
            print("S:Terminating...\n")
            nil
         }
      }
      case x_2 : NEGOTIATE => {
         print("S:Received type NEGOTIATE from x_2\n")
         print("S:Sending NEGOTIATE through channel c_S_B_5\n")
         send(c_S_B_5,NEGOTIATE()) >> {
            print("S:go to loop RecS_4\n")
            loop(RecS_4)
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
            rec(RecS_4){
               print("S:entering loop RecS_4\n")
               receive(c_S_B_2) {
                  (x:OFFER) =>
                  print("S:Receive type OFFER through channel c_S_B_2\n")
                  print("S:Sending OFFER through channel c_S_A_3\n")
                  send(c_S_A_3,x) >> {
                     receive(c_S_A_4) {
                        (x:BUY|CANCEL|NEGOTIATE) =>
                        print("S:Receive type BUY|CANCEL|NEGOTIATE through channel c_S_A_4\n")
                        s_2(x,c_S_A_5,c_S_B_3,c_S_B_4,c_S_B_5)
                     }
                  }
               }
            }
         }
      }
   }
}