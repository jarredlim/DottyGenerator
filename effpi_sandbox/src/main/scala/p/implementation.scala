package effpi_sandbox.p.implementation

import effpi_sandbox.caseclass._
import effpi_sandbox.p.types._
import effpi.process._
import effpi.process.dsl._
import effpi.channel.{InChannel, OutChannel}
import scala.concurrent.duration.Duration
import effpi.recurse._
import java.util.Date

implicit val timeout: Duration = Duration("60 seconds")

   def p(
      c_p_q_1: OutChannel[Data],
      c_p_r_1: OutChannel[Data]
   ):p[c_p_q_1.type,c_p_r_1.type] ={
      if(false){throw Exception("Some exception")}
      print("p:Sending Data through channel c_p_q_1\n")
      send(c_p_q_1,Data(-1)) >> {
         if(false){throw Exception("Some exception")}
         print("p:Sending Data through channel c_p_r_1\n")
         send(c_p_r_1,Data(-1)) >> {
            print("p:Terminating...\n")
            nil
         }
      }
   }



