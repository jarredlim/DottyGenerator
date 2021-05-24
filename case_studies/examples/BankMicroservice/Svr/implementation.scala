package sandbox.Svr.implementation

import sandbox.caseclass._
import sandbox.Svr.types._
import effpi.process._
import effpi.process.dsl._
import effpi.channel.{InChannel, OutChannel}
import scala.concurrent.duration.Duration
import effpi.recurse._
import java.util.Date

implicit val timeout: Duration = Duration("6000 seconds")

   private def svr_4(
      response: Success|Fail,
      c_Svr_Client_3: OutChannel[Success],
      c_Svr_SvrVer_3: OutChannel[Continue],
      c_Svr_Client_4: OutChannel[Cancel|Retry],
      c_Svr_SvrVer_4: OutChannel[Cancel],
      c_Svr_SvrAct_8: OutChannel[Cancel],
      c_Svr_SvrAct_9: OutChannel[Retry]
   ):Svr_4[response.type,c_Svr_Client_3.type,c_Svr_SvrVer_3.type,c_Svr_Client_4.type,c_Svr_SvrVer_4.type,c_Svr_SvrAct_8.type,c_Svr_SvrAct_9.type] =
      response match {
         case response : Success => {
            print("Svr:Actual type Received from x_4: Success\n")
            print("Svr:Sending Success through channel c_Svr_Client_3\n")
            send(c_Svr_Client_3,Success(response.message)) >> {
               print("Svr:Sending Continue through channel c_Svr_SvrVer_3\n")
               send(c_Svr_SvrVer_3,Continue()) >> {
                  print("Svr:go to loop RecSvr_8\n")
                  loop(RecSvr_8)
               }
            }
         }
         case response : Fail => {
            print("Svr:Actual type Received from x_4: Fail\n")
            print("Svr:Making selection through channel c_Svr_Client_4\n")
            if(response.errorCode == 202 || response.errorCode == 203){
               print("Svr:Sending Cancel through channel c_Svr_Client_4\n")
               send(c_Svr_Client_4,Cancel()) >> {
                  print("Svr:Sending Cancel through channel c_Svr_SvrVer_4\n")
                  send(c_Svr_SvrVer_4,Cancel()) >> {
                     print("Svr:Sending Cancel through channel c_Svr_SvrAct_8\n")
                     send(c_Svr_SvrAct_8,Cancel()) >> {
                        print("Svr:Terminating...\n")
                        nil
                     }
                  }
               }
            }
            else{
               print("Svr:Sending Retry through channel c_Svr_Client_4\n")
               send(c_Svr_Client_4,Retry(response.errorMessage)) >> {
                  print("Svr:Sending Continue through channel c_Svr_SvrVer_3\n")
                  send(c_Svr_SvrVer_3,Continue()) >> {
                     print("Svr:Sending Retry through channel c_Svr_SvrAct_9\n")
                     send(c_Svr_SvrAct_9,Retry(response.errorMessage)) >> {
                        print("Svr:go to loop RecSvr_8\n")
                        loop(RecSvr_8)
                     }
                  }
               }
            }
         }
      }


   private def svr_5(
      response: Success|Fail,
      c_Svr_Client_3: OutChannel[Success],
      c_Svr_SvrVer_3: OutChannel[Continue],
      c_Svr_Client_4: OutChannel[Cancel|Retry],
      c_Svr_SvrVer_4: OutChannel[Cancel],
      c_Svr_SvrAct_8: OutChannel[Cancel],
      c_Svr_SvrAct_9: OutChannel[Retry]
   ):Svr_5[response.type,c_Svr_Client_3.type,c_Svr_SvrVer_3.type,c_Svr_Client_4.type,c_Svr_SvrVer_4.type,c_Svr_SvrAct_8.type,c_Svr_SvrAct_9.type] =
      response match {
         case response : Success => {
            print("Svr:Actual type Received from x_5: Success\n")
            print("Svr:Sending Success through channel c_Svr_Client_3\n")
            send(c_Svr_Client_3,Success(response.message)) >> {
               print("Svr:Sending Continue through channel c_Svr_SvrVer_3\n")
               send(c_Svr_SvrVer_3,Continue()) >> {
                  print("Svr:go to loop RecSvr_8\n")
                  loop(RecSvr_8)
               }
            }
         }
         case response : Fail => {
            print("Svr:Actual type Received from x_5: Fail\n")
            val r = scala.util.Random(System.currentTimeMillis())
            if(response.errorCode == 202 || response.errorCode == 203){
               print("Svr:Sending Cancel through channel c_Svr_Client_4\n")
               send(c_Svr_Client_4,Cancel()) >> {
                  print("Svr:Sending Cancel through channel c_Svr_SvrVer_4\n")
                  send(c_Svr_SvrVer_4,Cancel()) >> {
                     print("Svr:Sending Cancel through channel c_Svr_SvrAct_8\n")
                     send(c_Svr_SvrAct_8,Cancel()) >> {
                        print("Svr:Terminating...\n")
                        nil
                     }
                  }
               }
            }
            else{
               print("Svr:Sending Retry through channel c_Svr_Client_4\n")
               send(c_Svr_Client_4,Retry(response.errorMessage)) >> {
                  print("Svr:Sending Continue through channel c_Svr_SvrVer_3\n")
                  send(c_Svr_SvrVer_3,Continue()) >> {
                     print("Svr:Sending Retry through channel c_Svr_SvrAct_9\n")
                     send(c_Svr_SvrAct_9,Retry(response.errorMessage)) >> {
                        print("Svr:go to loop RecSvr_8\n")
                        loop(RecSvr_8)
                     }
                  }
               }
            }
         }
      }


   private def svr_3(
      username: String, 
      action: Withdraw1|Deposit1|Cancel,
      c_Svr_SvrAct_4: OutChannel[Withdraw2],
      c_Svr_SvrAct_7: InChannel[Success|Fail],
      c_Svr_Client_3: OutChannel[Success],
      c_Svr_SvrVer_3: OutChannel[Continue],
      c_Svr_Client_4: OutChannel[Cancel|Retry],
      c_Svr_SvrVer_4: OutChannel[Cancel],
      c_Svr_SvrAct_8: OutChannel[Cancel],
      c_Svr_SvrAct_9: OutChannel[Retry],
      c_Svr_SvrAct_5: OutChannel[Deposit2],
      c_Svr_SvrAct_6: OutChannel[Cancel]
   ):Svr_3[action.type,c_Svr_SvrAct_4.type,c_Svr_SvrAct_7.type,c_Svr_Client_3.type,c_Svr_SvrVer_3.type,c_Svr_Client_4.type,c_Svr_SvrVer_4.type,c_Svr_SvrAct_8.type,c_Svr_SvrAct_9.type,c_Svr_SvrAct_5.type,c_Svr_SvrAct_6.type] =
      action match {
         case action : Withdraw1 => {
            print("Svr:Actual type Received from x_3: Withdraw1\n")
            print("Svr:Sending Withdraw2 through channel c_Svr_SvrAct_4\n")
            send(c_Svr_SvrAct_4,Withdraw2(username, action.amount)) >> {
               print("Svr:Sending Continue through channel c_Svr_SvrVer_3\n")
               send(c_Svr_SvrVer_3,Continue()) >> {
                  receive(c_Svr_SvrAct_7) {
                     (x_4:Success|Fail) =>
                     print("Svr:Receive type Success|Fail through channel c_Svr_SvrAct_7\n")
                     svr_4(x_4,c_Svr_Client_3,c_Svr_SvrVer_3,c_Svr_Client_4,c_Svr_SvrVer_4,c_Svr_SvrAct_8,c_Svr_SvrAct_9)
                  }
               }
            }
         }
         case action : Deposit1 => {
            print("Svr:Actual type Received from x_3: Deposit1\n")
            print("Svr:Sending Deposit2 through channel c_Svr_SvrAct_5\n")
            send(c_Svr_SvrAct_5,Deposit2(username,action.amount)) >> {
               print("Svr:Sending Continue through channel c_Svr_SvrVer_3\n")
               send(c_Svr_SvrVer_3,Continue()) >> {
                  receive(c_Svr_SvrAct_7) {
                     (x_5:Success|Fail) =>
                     print("Svr:Receive type Success|Fail through channel c_Svr_SvrAct_7\n")
                     svr_5(x_5,c_Svr_Client_3,c_Svr_SvrVer_3,c_Svr_Client_4,c_Svr_SvrVer_4,c_Svr_SvrAct_8,c_Svr_SvrAct_9)
                  }
               }
            }
         }
         case action : Cancel => {
            print("Svr:Actual type Received from x_3: Cancel\n")
            print("Svr:Sending Cancel through channel c_Svr_SvrVer_4\n")
            send(c_Svr_SvrVer_4,Cancel()) >> {
               print("Svr:Sending Cancel through channel c_Svr_SvrAct_6\n")
               send(c_Svr_SvrAct_6,Cancel()) >> {
                  print("Svr:Terminating...\n")
                  nil
               }
            }
         }
      }


   private def svr_2(
      username : String, 
      verified: Success|Fail,
      c_Svr_SvrAct_1: OutChannel[Continue],
      c_Svr_Client_5: InChannel[Withdraw1|Deposit1|Cancel],
      c_Svr_SvrAct_4: OutChannel[Withdraw2],
      c_Svr_SvrAct_7: InChannel[Success|Fail],
      c_Svr_Client_3: OutChannel[Success],
      c_Svr_SvrVer_3: OutChannel[Continue],
      c_Svr_Client_4: OutChannel[Cancel|Retry],
      c_Svr_SvrVer_4: OutChannel[Cancel],
      c_Svr_SvrAct_8: OutChannel[Cancel],
      c_Svr_SvrAct_9: OutChannel[Retry],
      c_Svr_SvrAct_5: OutChannel[Deposit2],
      c_Svr_SvrAct_6: OutChannel[Cancel],
      c_Svr_SvrVer_5: OutChannel[Cancel],
      c_Svr_SvrAct_2: OutChannel[Cancel],
      c_Svr_SvrVer_6: OutChannel[Retry],
      c_Svr_SvrAct_3: OutChannel[Retry]
   ):Svr_2[verified.type,c_Svr_SvrAct_1.type,c_Svr_Client_5.type,c_Svr_SvrAct_4.type,c_Svr_SvrAct_7.type,c_Svr_Client_3.type,c_Svr_SvrVer_3.type,c_Svr_Client_4.type,c_Svr_SvrVer_4.type,c_Svr_SvrAct_8.type,c_Svr_SvrAct_9.type,c_Svr_SvrAct_5.type,c_Svr_SvrAct_6.type,c_Svr_SvrVer_5.type,c_Svr_SvrAct_2.type,c_Svr_SvrVer_6.type,c_Svr_SvrAct_3.type] =
      verified match {
         case verified : Success => {
            print("Svr:Actual type Received from x_2: Success\n")
            print("Svr:Sending Success through channel c_Svr_Client_3\n")
            send(c_Svr_Client_3,Success(verified.message)) >> {
               print("Svr:Sending Continue through channel c_Svr_SvrAct_1\n")
               send(c_Svr_SvrAct_1,Continue()) >> {
                  rec(RecSvr_8){
                     print("Svr:entering loop RecSvr_8\n")
                     receive(c_Svr_Client_5) {
                        (x_3:Withdraw1|Deposit1|Cancel) =>
                        print("Svr:Receive type Withdraw1|Deposit1|Cancel through channel c_Svr_Client_5\n")
                        svr_3(username, x_3,c_Svr_SvrAct_4,c_Svr_SvrAct_7,c_Svr_Client_3,c_Svr_SvrVer_3,c_Svr_Client_4,c_Svr_SvrVer_4,c_Svr_SvrAct_8,c_Svr_SvrAct_9,c_Svr_SvrAct_5,c_Svr_SvrAct_6)
                     }
                  }
               }
            }
         }
         case verified : Fail => {
            print("Svr:Actual type Received from x_2: Fail\n")
            print("Svr:Making selection through channel c_Svr_Client_4\n")
            if(verified.errorCode == 403 || verified.errorCode == 404 ){
               print("Svr:Sending Cancel through channel c_Svr_Client_4\n")
               send(c_Svr_Client_4,Cancel()) >> {
                  print("Svr:Sending Cancel through channel c_Svr_SvrVer_5\n")
                  send(c_Svr_SvrVer_5,Cancel()) >> {
                     print("Svr:Sending Cancel through channel c_Svr_SvrAct_2\n")
                     send(c_Svr_SvrAct_2,Cancel()) >> {
                        print("Svr:Terminating...\n")
                        nil
                     }
                  }
               }
            }
            else{
               print("Svr:Sending Retry through channel c_Svr_Client_4\n")
               send(c_Svr_Client_4,Retry(verified.errorMessage)) >> {
                  print("Svr:Sending Retry through channel c_Svr_SvrVer_6\n")
                  send(c_Svr_SvrVer_6,Retry(verified.errorMessage)) >> {
                     print("Svr:Sending Retry through channel c_Svr_SvrAct_3\n")
                     send(c_Svr_SvrAct_3,Retry(verified.errorMessage)) >> {
                        print("Svr:go to loop RecSvr_1\n")
                        loop(RecSvr_1)
                     }
                  }
               }
            }
         }
      }


   def svr(
      c_Svr_Client_1: InChannel[Connect],
      c_Svr_Client_2: InChannel[Login],
      c_Svr_SvrVer_1: OutChannel[Login],
      c_Svr_SvrVer_2: InChannel[Success|Fail],
      c_Svr_SvrAct_1: OutChannel[Continue],
      c_Svr_Client_5: InChannel[Withdraw1|Deposit1|Cancel],
      c_Svr_SvrAct_4: OutChannel[Withdraw2],
      c_Svr_SvrAct_7: InChannel[Success|Fail],
      c_Svr_Client_3: OutChannel[Success],
      c_Svr_SvrVer_3: OutChannel[Continue],
      c_Svr_Client_4: OutChannel[Cancel|Retry],
      c_Svr_SvrVer_4: OutChannel[Cancel],
      c_Svr_SvrAct_8: OutChannel[Cancel],
      c_Svr_SvrAct_9: OutChannel[Retry],
      c_Svr_SvrAct_5: OutChannel[Deposit2],
      c_Svr_SvrAct_6: OutChannel[Cancel],
      c_Svr_SvrVer_5: OutChannel[Cancel],
      c_Svr_SvrAct_2: OutChannel[Cancel],
      c_Svr_SvrVer_6: OutChannel[Retry],
      c_Svr_SvrAct_3: OutChannel[Retry]
   ):Svr[c_Svr_Client_1.type,c_Svr_Client_2.type,c_Svr_SvrVer_1.type,c_Svr_SvrVer_2.type,c_Svr_SvrAct_1.type,c_Svr_Client_5.type,c_Svr_SvrAct_4.type,c_Svr_SvrAct_7.type,c_Svr_Client_3.type,c_Svr_SvrVer_3.type,c_Svr_Client_4.type,c_Svr_SvrVer_4.type,c_Svr_SvrAct_8.type,c_Svr_SvrAct_9.type,c_Svr_SvrAct_5.type,c_Svr_SvrAct_6.type,c_Svr_SvrVer_5.type,c_Svr_SvrAct_2.type,c_Svr_SvrVer_6.type,c_Svr_SvrAct_3.type] ={
      receive(c_Svr_Client_1) {
         (x:Connect) =>
         print("Svr:Receive type Connect through channel c_Svr_Client_1\n")
         rec(RecSvr_1){
            print("Svr:entering loop RecSvr_1\n")
            receive(c_Svr_Client_2) {
               (loginDetails:Login) =>
               print("Svr:Receive type Login through channel c_Svr_Client_2\n")
               print("Svr:Sending Login through channel c_Svr_SvrVer_1\n")
               send(c_Svr_SvrVer_1,loginDetails) >> {
                  receive(c_Svr_SvrVer_2) {
                     (x_2:Success|Fail) =>
                     print("Svr:Receive type Success|Fail through channel c_Svr_SvrVer_2\n")
                     svr_2(loginDetails.username, x_2,c_Svr_SvrAct_1,c_Svr_Client_5,c_Svr_SvrAct_4,c_Svr_SvrAct_7,c_Svr_Client_3,c_Svr_SvrVer_3,c_Svr_Client_4,c_Svr_SvrVer_4,c_Svr_SvrAct_8,c_Svr_SvrAct_9,c_Svr_SvrAct_5,c_Svr_SvrAct_6,c_Svr_SvrVer_5,c_Svr_SvrAct_2,c_Svr_SvrVer_6,c_Svr_SvrAct_3)
                  }
               }
            }
         }
      }
   }



