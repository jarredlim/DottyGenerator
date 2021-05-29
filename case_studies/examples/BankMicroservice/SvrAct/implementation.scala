package sandbox.SvrAct.implementation

import sandbox.caseclass._
import sandbox.SvrAct.types._
import effpi.process._
import effpi.process.dsl._
import effpi.channel.{InChannel, OutChannel}
import scala.concurrent.duration.Duration
import effpi.recurse._
import java.util.Date

implicit val timeout: Duration = Duration("6000 seconds")

import scala.io.Source
import java.io.{FileWriter, File}
import io.circe._, io.circe.parser._
import io.circe.syntax._
import java.time.{Duration => Durations, _}
import io.circe.Decoder.Result

private val projPath = System.getProperty("user.dir")
private val transactionsPath = projPath + "/case_studies/sandbox/src/main/scala/BankMicroservice/SvrAct/transactions.json"

   private def svrAct_4(
      x_4: Retry|Cancel
   ):SvrAct_4[x_4.type] =
      x_4 match {
         case x_4 : Retry => {
            print("SvrAct:Actual type Received from x_4: Retry\n")
            print("SvrAct:go to loop RecSvrAct_6\n")
            loop(RecSvrAct_6)
         }
         case x_4 : Cancel => {
            print("SvrAct:Actual type Received from x_4: Cancel\n")
            print("SvrAct:Terminating...\n")
            nil
         }
      }


   private def svrAct_5(
      x_5: Retry|Cancel
   ):SvrAct_5[x_5.type] =
      x_5 match {
         case x_5 : Retry => {
            print("SvrAct:Actual type Received from x_5: Retry\n")
            print("SvrAct:go to loop RecSvrAct_6\n")
            loop(RecSvrAct_6)
         }
         case x_5 : Cancel => {
            print("SvrAct:Actual type Received from x_5: Cancel\n")
            print("SvrAct:Terminating...\n")
            nil
         }
      }


   private def svrAct_3(
      action: Withdraw2|Deposit2|Cancel,
      c_SvrAct_Svr_3: OutChannel[Success|Fail],
      c_SvrAct_Svr_4: InChannel[Retry|Cancel]
   ):SvrAct_3[action.type,c_SvrAct_Svr_3.type,c_SvrAct_Svr_4.type] =
      action match {
         case action : Withdraw2 => {
            print("SvrAct:Actual type Received from x_3: Withdraw2\n")
            val (succeed, errorCode, errorMessage) = performAction(action.username, action.amount, true)
            print("SvrAct:Making selection through channel c_SvrAct_Svr_3\n")
            if(succeed){
               print("SvrAct:Sending Success through channel c_SvrAct_Svr_3\n")
               send(c_SvrAct_Svr_3,Success(errorMessage)) >> {
                  print("SvrAct:go to loop RecSvrAct_6\n")
                  loop(RecSvrAct_6)
               }
            }
            else{
               print("SvrAct:Sending Fail through channel c_SvrAct_Svr_3\n")
               send(c_SvrAct_Svr_3,Fail(errorCode,errorMessage)) >> {
                  receive(c_SvrAct_Svr_4) {
                     (x_4:Retry|Cancel) =>
                     print("SvrAct:Receive type Retry|Cancel through channel c_SvrAct_Svr_4\n")
                     svrAct_4(x_4)
                  }
               }
            }
         }
         case action : Deposit2 => {
            print("SvrAct:Actual type Received from x_3: Deposit2\n")
            val (succeed, errorCode, errorMessage) = performAction(action.username, action.amount, false)
            print("SvrAct:Making selection through channel c_SvrAct_Svr_3\n")
            if(succeed){
               print("SvrAct:Sending Success through channel c_SvrAct_Svr_3\n")
               send(c_SvrAct_Svr_3,Success(errorMessage)) >> {
                  print("SvrAct:go to loop RecSvrAct_6\n")
                  loop(RecSvrAct_6)
               }
            }
            else{
               print("SvrAct:Sending Fail through channel c_SvrAct_Svr_3\n")
               send(c_SvrAct_Svr_3,Fail(errorCode,errorMessage)) >> {
                  receive(c_SvrAct_Svr_4) {
                     (x_5:Retry|Cancel) =>
                     print("SvrAct:Receive type Retry|Cancel through channel c_SvrAct_Svr_4\n")
                     svrAct_5(x_5)
                  }
               }
            }
         }
         case action : Cancel => {
            print("SvrAct:Actual type Received from x_3: Cancel\n")
            print("SvrAct:Terminating...\n")
            nil
         }
      }


   private def svrAct_2(
      x_2: Retry|Cancel|Continue,
      c_SvrAct_Svr_2: InChannel[Withdraw2|Deposit2|Cancel],
      c_SvrAct_Svr_3: OutChannel[Success|Fail],
      c_SvrAct_Svr_4: InChannel[Retry|Cancel]
   ):SvrAct_2[x_2.type,c_SvrAct_Svr_2.type,c_SvrAct_Svr_3.type,c_SvrAct_Svr_4.type] =
      x_2 match {
         case x_2 : Retry => {
            print("SvrAct:Actual type Received from x_2: Retry\n")
            print("SvrAct:go to loop RecSvrAct_0\n")
            loop(RecSvrAct_0)
         }
         case x_2 : Cancel => {
            print("SvrAct:Actual type Received from x_2: Cancel\n")
            print("SvrAct:Terminating...\n")
            nil
         }
         case x_2 : Continue => {
            print("SvrAct:Actual type Received from x_2: Continue\n")
            rec(RecSvrAct_6){
               print("SvrAct:entering loop RecSvrAct_6\n")
               receive(c_SvrAct_Svr_2) {
                  (x_3:Withdraw2|Deposit2|Cancel) =>
                  print("SvrAct:Receive type Withdraw2|Deposit2|Cancel through channel c_SvrAct_Svr_2\n")
                  svrAct_3(x_3,c_SvrAct_Svr_3,c_SvrAct_Svr_4)
               }
            }
         }
      }


   def svrAct(
      c_SvrAct_Svr_1: InChannel[Retry|Cancel|Continue],
      c_SvrAct_Svr_2: InChannel[Withdraw2|Deposit2|Cancel],
      c_SvrAct_Svr_3: OutChannel[Success|Fail],
      c_SvrAct_Svr_4: InChannel[Retry|Cancel]
   ):SvrAct[c_SvrAct_Svr_1.type,c_SvrAct_Svr_2.type,c_SvrAct_Svr_3.type,c_SvrAct_Svr_4.type] ={
      rec(RecSvrAct_0){
         print("SvrAct:entering loop RecSvrAct_0\n")
         receive(c_SvrAct_Svr_1) {
            (x_2:Retry|Cancel|Continue) =>
            print("SvrAct:Receive type Retry|Cancel|Continue through channel c_SvrAct_Svr_1\n")
            svrAct_2(x_2,c_SvrAct_Svr_2,c_SvrAct_Svr_3,c_SvrAct_Svr_4)
         }
      }
   }
   
   
   private def performAction(username: String, amount: Int, isWithdraw: Boolean) = {
     val transactions: Json = parse(Source.fromFile(transactionsPath).getLines.mkString).getOrElse(Json.Null)
     val cursor: HCursor = transactions.hcursor
     val users = cursor.downField("users").as[List[Json]].getOrElse(List())
     var newUsers = users
     var isValidTransaction = false
     var errorCode = 201
     var errorMessage = "User Not Found"
     for (i <- 0 until users.size){
        var user = users(i)
        val ucursor: HCursor = user.hcursor
        if (ucursor.downField("username").as[String].right.getOrElse(null) == username){
          
          var balance = ucursor.downField("balance").as[JsonNumber].right.getOrElse(null).toInt.getOrElse(-1)
          var history = ucursor.downField("transactions").as[List[String]].getOrElse(List())
          val currentTime = LocalDateTime.now()
          
          var transactionsCountToday = 0
          var withdrawAmountThisWeek = if (isWithdraw) amount else 0
          var depositAmountThisWeek = if (!isWithdraw) amount else 0
          
          
          for(j <- 0 until history.size){
              val entry = history(j).split(",")
              val entryTime = LocalDateTime.parse(entry(2))
              if(Durations.between(entryTime, currentTime).toHours() <= 24){
                   transactionsCountToday = transactionsCountToday + 1
              }
              
              if(Durations.between(entryTime, currentTime).toDays() <= 7){
                   if(entry(0) == "Withdraw"){
                       withdrawAmountThisWeek = withdrawAmountThisWeek + entry(1).toInt
                   }else{
                       depositAmountThisWeek = depositAmountThisWeek + entry(1).toInt
                   }
              }
              
          }
          
          if(transactionsCountToday >= 10){
              errorCode = 202
              errorMessage = "Daily allowed transaction limit reached"
          }else if(isWithdraw && amount > 500){
            errorCode = 203
            errorMessage = "Withdraw too much per transaction!"
          }else if(isWithdraw && amount > balance){
            errorCode = 206
            errorMessage = s"Balance Insuffice! Balance:${balance}"
          }else if(withdrawAmountThisWeek > 1000){
            errorCode = 204
            errorMessage = s"Exceeded weekly withdraw limit! Balance:${balance}, Amount Withdrawn This Week: ${withdrawAmountThisWeek - amount}"
          }else if(depositAmountThisWeek > 5000){
            errorCode = 207
            errorMessage = s"Exceeded weekly deposit limit! Balance:${balance}, Amount Deposited This Week: ${ depositAmountThisWeek
              - amount}"
          }else{
             errorCode = 300
             var transactionType = ""
             isValidTransaction = true
             if(isWithdraw){
                 transactionType = "Withdraw"
                 balance = balance - amount
                 errorMessage = s"Success! Balance:${balance}, Amount Withdrawn This Week: ${withdrawAmountThisWeek}"
             }else{
                 transactionType = "Deposit"
                 balance = balance + amount
                 errorMessage = s"Success! Balance:${balance}, Amount Deposited This Week: ${depositAmountThisWeek}"
             }
             var newEntry = s"${transactionType},${amount},${currentTime.toString()}"
             history = newEntry :: history
             var ncursor = ucursor.downField("transactions").set(history.asJson)
             ncursor = ncursor.top.getOrElse(Json.Null).hcursor.downField("balance").set(balance.asJson)
             val newUsers = users.updated(i,ncursor.top.getOrElse(Json.Null))
             ncursor = cursor.downField("users").set(newUsers.asJson)
             val fileWriter = new FileWriter(new File(transactionsPath))
             fileWriter.write(ncursor.top.getOrElse(Json.Null).toString())
             fileWriter.close()
          }
        }
     }
     (isValidTransaction, errorCode, errorMessage)
   }



