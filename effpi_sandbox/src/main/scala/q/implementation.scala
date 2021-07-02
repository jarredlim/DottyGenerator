package effpi_sandbox.q.implementation

import effpi_sandbox.caseclass._
import effpi_sandbox.q.types._
import effpi.process._
import effpi.process.dsl._
import effpi.channel.{InChannel, OutChannel}
import scala.concurrent.duration.Duration
import effpi.recurse._
import java.util.Date

implicit val timeout: Duration = Duration("60 seconds")

   private def q_2(
      x_2: Ok|req,
      c_q_r_2: OutChannel[Ko|Data]
   ):q_2[x_2.type,c_q_r_2.type] =
      x_2 match {
         case x_2 : Ok => {
            print("q:Actual type Received from x_2: Ok\n")
            print("q:Terminating...\n")
            nil
         }
         case x_2 : req => {
            print("q:Actual type Received from x_2: req\n")
            val r = scala.util.Random(System.currentTimeMillis())
            val decision = r.nextInt(2)
            print("q:Making selection through channel c_q_r_2\n")
            if(decision == 0){
               if(false){throw Exception("Some exception")}
               print("q:Sending Ko through channel c_q_r_2\n")
               send(c_q_r_2,Ko(-1)) >> {
                  print("q:Terminating...\n")
                  nil
               }
            }
            else{
               if(false){throw Exception("Some exception")}
               print("q:Sending Data through channel c_q_r_2\n")
               send(c_q_r_2,Data(-1)) >> {
                  print("q:Terminating...\n")
                  nil
               }
            }
         }
      }


   private def q_3(
      x_3: req|Ok,
      c_q_r_2: OutChannel[Ko|Data]
   ):q_3[x_3.type,c_q_r_2.type] =
      x_3 match {
         case x_3 : req => {
            print("q:Actual type Received from x_3: req\n")
            val r = scala.util.Random(System.currentTimeMillis())
            val decision = r.nextInt(2)
            print("q:Making selection through channel c_q_r_2\n")
            if(decision == 0){
               if(false){throw Exception("Some exception")}
               print("q:Sending Ko through channel c_q_r_2\n")
               send(c_q_r_2,Ko(-1)) >> {
                  print("q:Terminating...\n")
                  nil
               }
            }
            else{
               if(false){throw Exception("Some exception")}
               print("q:Sending Data through channel c_q_r_2\n")
               send(c_q_r_2,Data(-1)) >> {
                  print("q:Terminating...\n")
                  nil
               }
            }
         }
         case x_3 : Ok => {
            print("q:Actual type Received from x_3: Ok\n")
            print("q:Terminating...\n")
            throw Exception("This branch should not be reached! Please check your communication!")
            nil
         }
      }


   def q(
      c_q_p_1: InChannel[Data],
      c_q_r_1: InChannel[Ok|req],
      c_q_r_2: OutChannel[Ko|Data]
   ):q[c_q_p_1.type,c_q_r_1.type,c_q_r_2.type] ={
      receiveErr(c_q_p_1) ({
         (x:Data) =>
         print("q:Received type Data through channel c_q_p_1\n")
         receiveErr(c_q_r_1) ({
            (x_2:Ok|req) =>
            print("q:Received type Ok|req through channel c_q_r_1\n")
            q_2(x_2,c_q_r_2)
         },
         {(err : Throwable) =>
            print("q:Receive Ok|req through channel c_q_r_1 TIMEOUT, activating new option\n")
            print("q:Terminating...\n")
            nil
         }, Duration("5 seconds"))
      },
      {(err : Throwable) =>
         print("q:Receive Data through channel c_q_p_1 TIMEOUT, activating new option\n")
         receiveErr(c_q_r_1) ({
            (x_3:req|Ok) =>
            print("q:Received type req|Ok through channel c_q_r_1\n")
            q_3(x_3,c_q_r_2)
         },
         {(err : Throwable) =>
            print("q:Receive req|Ok through channel c_q_r_1 TIMEOUT, activating new option\n")
            print("q:Terminating...\n")
            nil
         }, Duration("5 seconds"))
      }, Duration("5 seconds"))
   }



