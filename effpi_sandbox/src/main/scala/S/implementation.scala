package effpi_sandbox.S.implementation

import effpi_sandbox.caseclass._
import effpi_sandbox.S.types._
import effpi.process._
import effpi.process.dsl._
import effpi.channel.{InChannel, OutChannel}
import scala.concurrent.duration.Duration
import effpi.recurse._
import java.util.Date

implicit val timeout: Duration = Duration("60 seconds")

   def s(
      c_S_A_1: InChannel[Query],
      c_S_A_2: OutChannel[Full|Available],
      c_S_A_3: InChannel[Reject|Confirm]
   ):S[c_S_A_1.type,c_S_A_2.type,c_S_A_3.type] ={
      rec(RecS_0){
         print("S:entering loop RecS_0\n")
         receive(c_S_A_1) {
            (x:Query) =>
            print("S:Receive type Query through channel c_S_A_1\n")
            val r = scala.util.Random(System.currentTimeMillis())
            val decision = r.nextInt(2)
            print("S:Making selection through channel c_S_A_2\n")
            if(decision == 0){
               print("S:Sending Full through channel c_S_A_2\n")
               send(c_S_A_2,Full()) >> {
                  print("S:go to loop RecS_0\n")
                  loop(RecS_0)
               }
            }
            else{
               print("S:Sending Available through channel c_S_A_2\n")
               send(c_S_A_2,Available(-1)) >> {
                  receive(c_S_A_3) {
                     (x:Reject|Confirm) =>
                     print("S:Receive type Reject|Confirm through channel c_S_A_3\n")
                     print("S:Terminating...\n")
                     nil
                  }
               }
            }
         }
      }
   }



