package effpi_sandbox.B.implementation

import effpi_sandbox.caseclass._
import effpi_sandbox.B.types._
import effpi.process._
import effpi.process.dsl._
import effpi.channel.{InChannel, OutChannel}
import scala.concurrent.duration.Duration
import effpi.recurse._
import java.util.Date

implicit val timeout: Duration = Duration("60 seconds")

   private def b_2(
      x_2: Full|Quote,
      c_B_A_3: OutChannel[OK|No]
   ):B_2[x_2.type,c_B_A_3.type] =
      x_2 match {
         case x_2 : Full => {
            print("B:Actual type Received from x_2: Full\n")
            print("B:go to loop RecB_0\n")
            loop(RecB_0)
         }
         case x_2 : Quote => {
            print("B:Actual type Received from x_2: Quote\n")
            val r = scala.util.Random(System.currentTimeMillis())
            val decision = r.nextInt(2)
            print("B:Making selection through channel c_B_A_3\n")
            if(decision == 0){
               print("B:Sending OK through channel c_B_A_3\n")
               send(c_B_A_3,OK(-1)) >> {
                  print("B:Terminating...\n")
                  nil
               }
            }
            else{
               print("B:Sending No through channel c_B_A_3\n")
               send(c_B_A_3,No()) >> {
                  print("B:Terminating...\n")
                  nil
               }
            }
         }
      }


   def b(
      c_B_A_1: OutChannel[Suggest],
      c_B_A_2: InChannel[Full|Quote],
      c_B_A_3: OutChannel[OK|No]
   ):B[c_B_A_1.type,c_B_A_2.type,c_B_A_3.type] ={
      rec(RecB_0){
         print("B:entering loop RecB_0\n")
         print("B:Sending Suggest through channel c_B_A_1\n")
         send(c_B_A_1,Suggest("REPLACE_ME")) >> {
            receive(c_B_A_2) {
               (x_2:Full|Quote) =>
               print("B:Receive type Full|Quote through channel c_B_A_2\n")
               b_2(x_2,c_B_A_3)
            }
         }
      }
   }



