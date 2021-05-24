package sandbox.SvrVer.implementation

import sandbox.caseclass._
import sandbox.SvrVer.types._
import effpi.process._
import effpi.process.dsl._
import effpi.channel.{InChannel, OutChannel}
import scala.concurrent.duration.Duration
import effpi.recurse._
import java.util.Date

import scala.io.Source
import java.io.{FileWriter, File}
import io.circe._, io.circe.parser._
import io.circe.syntax._
import java.time.{Duration => Durations, _}
import io.circe.Decoder.Result

private val projPath = System.getProperty("user.dir")
private val credentialsPath = projPath + "/case_studies/sandbox/src/main/scala/BankMicroservice/SvrVer/credentials.json"

implicit val timeout: Duration = Duration("6000 seconds")

   private def svrVer_3(
      x_3: Continue|Cancel
   ):SvrVer_3[x_3.type] =
      x_3 match {
         case x_3 : Continue => {
            print("SvrVer:Actual type Received from x_3: Continue\n")
            print("SvrVer:go to loop RecSvrVer_4\n")
            loop(RecSvrVer_4)
         }
         case x_3 : Cancel => {
            print("SvrVer:Actual type Received from x_3: Cancel\n")
            print("SvrVer:Terminating...\n")
            nil
         }
      }


   private def svrVer_2(
      x_2: Cancel|Continue,
      c_SvrVer_Svr_3: InChannel[Continue|Cancel]
   ):SvrVer_2[x_2.type,c_SvrVer_Svr_3.type] =
      x_2 match {
         case x_2 : Cancel => {
            print("SvrVer:Actual type Received from x_2: Cancel\n")
            print("SvrVer:Terminating...\n")
            nil
         }
         case x_2 : Continue => {
            print("SvrVer:Actual type Received from x_2: Continue\n")
            receive(c_SvrVer_Svr_3) {
               (x_3:Continue|Cancel) =>
               print("SvrVer:Receive type Continue|Cancel through channel c_SvrVer_Svr_3\n")
               svrVer_3(x_3)
            }
         }
      }


   private def svrVer_4(
      x_4: Retry|Cancel
   ):SvrVer_4[x_4.type] =
      x_4 match {
         case x_4 : Retry => {
            print("SvrVer:Actual type Received from x_4: Retry\n")
            print("SvrVer:go to loop RecSvrVer_0\n")
            loop(RecSvrVer_0)
         }
         case x_4 : Cancel => {
            print("SvrVer:Actual type Received from x_4: Cancel\n")
            print("SvrVer:Terminating...\n")
            nil
         }
      }


   def svrVer(
      c_SvrVer_Svr_1: InChannel[Login],
      c_SvrVer_Svr_2: OutChannel[Success|Fail],
      c_SvrVer_Svr_3: InChannel[Continue|Cancel],
      c_SvrVer_Svr_4: InChannel[Retry|Cancel]
   ):SvrVer[c_SvrVer_Svr_1.type,c_SvrVer_Svr_2.type,c_SvrVer_Svr_3.type,c_SvrVer_Svr_4.type] ={
      rec(RecSvrVer_0){
         print("SvrVer:entering loop RecSvrVer_0\n")
         receive(c_SvrVer_Svr_1) {
            (loginDetails :Login) =>
            print("SvrVer:Receive type Login through channel c_SvrVer_Svr_1\n")
            print("SvrVer:Making selection through channel c_SvrVer_Svr_2\n")
            val (succeed, errorCode, message) = checkCredentials(loginDetails.username, loginDetails.pw)
            if(succeed){
               print("SvrVer:Sending Success through channel c_SvrVer_Svr_2\n")
               send(c_SvrVer_Svr_2,Success(message)) >> {
                  rec(RecSvrVer_4){
                     print("SvrVer:entering loop RecSvrVer_4\n")
                     receive(c_SvrVer_Svr_3) {
                        (x_2:Cancel|Continue) =>
                        print("SvrVer:Receive type Cancel|Continue through channel c_SvrVer_Svr_3\n")
                        svrVer_2(x_2,c_SvrVer_Svr_3)
                     }
                  }
               }
            }
            else{
               print("SvrVer:Sending Fail through channel c_SvrVer_Svr_2\n")
               send(c_SvrVer_Svr_2,Fail(errorCode,message)) >> {
                  receive(c_SvrVer_Svr_4) {
                     (x_4:Retry|Cancel) =>
                     print("SvrVer:Receive type Retry|Cancel through channel c_SvrVer_Svr_4\n")
                     svrVer_4(x_4)
                  }
               }
            }
         }
      }
   }
   
   private def checkCredentials(username: String, pw: String) = {
     val credentials: Json = parse(Source.fromFile(credentialsPath).getLines.mkString).getOrElse(Json.Null)
     val cursor: HCursor = credentials.hcursor
     val users = cursor.downField("users").as[List[Json]].getOrElse(List())
     var newUsers = users
     var isValidCredentials = false
     var errorCode = 401
     var errorMessage = "User Not Found"
     for (i <- 0 until users.size){
        var user = users(i)
        val ucursor: HCursor = user.hcursor
        if (ucursor.downField("username").as[String].right.getOrElse(null) == username){
          
          var lastFailTime = LocalDateTime.parse(ucursor.downField("lastFailed").as[String].right.getOrElse(""))
          var failedAttempts = ucursor.downField("failedAttempts").as[JsonNumber].right.getOrElse(null).toInt.getOrElse(-1)
          var banned = ucursor.downField("banned").as[Boolean].right.getOrElse(false)
          val currentTime = LocalDateTime.now()
          
          if(banned){
            errorCode = 403
            errorMessage = "Blacklisted User"
          }
          else if (Durations.between(lastFailTime, currentTime).toMinutes() <= 10 && failedAttempts >= 3){
              errorCode = 404
              errorMessage = "Too many failed attempts"
          }
          else{
             if(failedAttempts >= 3 && Durations.between(lastFailTime, currentTime).toMinutes() > 10){
                failedAttempts = 0
             }
             if(ucursor.downField("pw").as[String].right.getOrElse(null) == pw){
                 isValidCredentials = true
                 failedAttempts = 0
                 errorCode = 202
                 errorMessage = "Suceed"
             }else{
                failedAttempts = failedAttempts + 1
                lastFailTime = currentTime
                errorCode = 402
                errorMessage = "Wrong Password"
             }
             var ncursor: ACursor = ucursor.downField("lastFailed").set(lastFailTime.toString().asJson)
             ncursor = ncursor.top.getOrElse(Json.Null).hcursor.downField("failedAttempts").set(failedAttempts.asJson)
             val newUsers = users.updated(i,ncursor.top.getOrElse(Json.Null))
             ncursor = cursor.downField("users").set(newUsers.asJson)
             val fileWriter = new FileWriter(new File(credentialsPath))
             fileWriter.write(ncursor.top.getOrElse(Json.Null).toString())
             fileWriter.close()
          }
        }
     }
     (isValidCredentials, errorCode, errorMessage)
   }
   
  //  implicit val localDateTimeFormat: Encoder[LocalDateTime] with Decoder[LocalDateTime] = new Encoder[LocalDateTime] with Decoder[LocalDateTime] {
  //   override def apply(a: LocalDateTime): Json = Encoder.encodeLong.apply(toMillis(a))
  //   override def apply(c: HCursor): Result[LocalDateTime] = Decoder.decodeLong.map(s => fromMillis(s)).apply(c)
  // }
  // 
  // def toMillis(ldt: LocalDateTime) = ldt.atZone(ZoneId.systemDefault).toInstant.toEpochMilli
  // def fromMillis(millis: Long) = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault).toLocalDateTime
   



