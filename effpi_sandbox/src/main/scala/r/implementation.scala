package effpi_sandbox.r.implementation

import effpi_sandbox.caseclass._
import effpi_sandbox.r.types._
import effpi.process._
import effpi.process.dsl._
import effpi.channel.{InChannel, OutChannel}
import scala.concurrent.duration.Duration
import effpi.recurse._
import java.util.Date

implicit val timeout: Duration = Duration("60 seconds")

   def r(
      c_r_p_1: InChannel[Data],
      c_r_q_1: OutChannel[Ok],
      c_r_q_2: OutChannel[req],
      c_r_q_3: InChannel[Ko|Data]
   ):r[c_r_p_1.type,c_r_q_1.type,c_r_q_2.type,c_r_q_3.type] ={
      receiveErr(c_r_p_1) ({
         (x:Data) =>
         print("r:Received type Data through channel c_r_p_1\n")
         if(false){throw Exception("Some exception")}
         print("r:Sending Ok through channel c_r_q_1\n")
         send(c_r_q_1,Ok(-1)) >> {
            print("r:Terminating...\n")
            nil
         }
      },
      {(err : Throwable) =>
         print("r:Receive Data through channel c_r_p_1 TIMEOUT, activating new option\n")
         if(false){throw Exception("Some exception")}
         print("r:Sending req through channel c_r_q_2\n")
         send(c_r_q_2,req(-1)) >> {
            receiveErr(c_r_q_3) ({
               (x:Ko|Data) =>
               print("r:Received type Ko|Data through channel c_r_q_3\n")
               print("r:Terminating...\n")
               nil
            },
            {(err : Throwable) =>
               print("r:Receive Ko|Data through channel c_r_q_3 TIMEOUT, activating new option\n")
               print("r:Terminating...\n")
               nil
            }, Duration("5 seconds"))
         }
      }, Duration("5 seconds"))
   }



