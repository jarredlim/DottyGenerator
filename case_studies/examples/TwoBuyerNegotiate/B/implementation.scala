package sandbox.B.implementation

import sandbox.caseclass._
import sandbox.B.types._
import effpi.process._
import effpi.process.dsl._
import effpi.channel.{InChannel, OutChannel}
import scala.concurrent.duration.Duration
import effpi.recurse._
import java.util.Date

implicit val timeout: Duration = Duration("30 seconds")
private var iteration = 0

private def b_2(
 x_2: NEGOTIATE|CONFIRM|CANCEL
):B_2[x_2.type] =
 x_2 match {
    case x_2 : NEGOTIATE => {
       print("B:Received type NEGOTIATE from x_2\n")
       print("B:go to loop RecB2\n")
       iteration = iteration + 1
       loop(RecB_2)
    }
    case x_2 : CONFIRM => {
       print("B:Received type CONFIRM from x_2\n")
       print("B:Terminating...\n")
       nil
    }
    case x_2 : CANCEL => {
       print("B:Received type CANCEL from x_2\n")
       print("B:Terminating...\n")
       nil
    }
 }


def b(
 c_B_S_1: InChannel[PRICE],
 c_B_S_2: OutChannel[OFFER],
 c_B_S_3: InChannel[NEGOTIATE|CONFIRM|CANCEL]
):B[c_B_S_1.type,c_B_S_2.type,c_B_S_3.type] ={
 receive(c_B_S_1) {
    (x:PRICE) =>
    print("B:Receive type PRICE through channel c_B_S_1\n")
    rec(RecB_2){
       print("B:entering loop RecB_2\n")
       print("B:Sending OFFER through channel c_B_S_2\n")
       send(c_B_S_2,OFFER(100 + 10 * iteration)) >> {
          receive(c_B_S_3) {
             (x:NEGOTIATE|CONFIRM|CANCEL) =>
             print("B:Receive type NEGOTIATE|CONFIRM|CANCEL through channel c_B_S_3\n")
             b_2(x)
          }
       }
    }
 }
}