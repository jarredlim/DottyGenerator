package sandbox.A.implementation

import sandbox.caseclass._
import sandbox.A.types._
import effpi.process._
import effpi.process.dsl._
import effpi.channel.{InChannel, OutChannel}
import scala.concurrent.duration.Duration
import effpi.recurse._
import java.util.Date

implicit val timeout: Duration = Duration("30 seconds")
private var iteration = 0

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
         rec(RecA_3){
            print("A:entering loop RecA_3\n")
            receive(c_A_S_3) {
               (x2:OFFER) =>
               print("A:Receive type OFFER through channel c_A_S_3\n")
               print("A:Making selection through channel c_A_S_4\n")
               if(x2.amount < x1.amount/2 && iteration < 10){
                  print("A:Sending NEGOTIATE through channel c_A_S_4\n")
                  iteration += 1
                  send(c_A_S_4,NEGOTIATE()) >> {
                     print("A:go to loop RecA_3\n")
                     loop(RecA_3)
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