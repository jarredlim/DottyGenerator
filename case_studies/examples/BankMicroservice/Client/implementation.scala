package sandbox.Client.implementation

import sandbox.caseclass._
import sandbox.Client.types._
import effpi.process._
import effpi.process.dsl._
import effpi.channel.{InChannel, OutChannel}
import scala.concurrent.duration.Duration
import effpi.recurse._
import java.util.Date
import java.util.concurrent.Semaphore

import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import scala.io.Source
import java.io.InputStream
import sandbox.Http.RootHandler

implicit val timeout: Duration = Duration("6000 seconds")
val semaphore = new Semaphore(0)

private var canRelease = false
private var possibleMessage = Map[String, List[String]]()
private var choice = ""
private var displayMessage = ""
private var payloads = List[String]()
private var server = HttpServer.create(new InetSocketAddress(8080), 0)

   private def client_3(
      x_3: Retry|Success|Cancel
   ):Client_3[x_3.type] =
      x_3 match {
         case x_3 : Retry => {
            displayMessage += s"Received Retry(message=${x_3.message})<br/>"
            print("Client:Actual type Received from x_3: Retry\n")
            print("Client:go to loop RecClient_8\n")
            loop(RecClient_8)
         }
         case x_3 : Success => {
            displayMessage += s"Received Success(message=${x_3.message})<br/>"
            print("Client:Actual type Received from x_3: Success\n")
            print("Client:go to loop RecClient_8\n")
            loop(RecClient_8)
         }
         case x_3 : Cancel => {
            displayMessage += s"Received Cancel()<br/>"
            print("Client:Actual type Received from x_3: Cancel\n")
            print("Client:Terminating...\n")
            server.stop(0)
            nil
         }
      }


   private def client_4(
      x_4: Retry|Success|Cancel
   ):Client_4[x_4.type] =
      x_4 match {
         case x_4 : Retry => {
            displayMessage += s"Received Retry(message=${x_4.message})<br/>"
            print("Client:Actual type Received from x_4: Retry\n")
            print("Client:go to loop RecClient_8\n")
            loop(RecClient_8)
         }
         case x_4 : Success => {
            displayMessage += s"Received Success(message=${x_4.message})<br/>"
            print("Client:Actual type Received from x_4: Success\n")
            print("Client:go to loop RecClient_8\n")
            loop(RecClient_8)
         }
         case x_4 : Cancel => {
            displayMessage += s"Received Cancel()<br/>"
            print("Client:Actual type Received from x_4: Cancel\n")
            print("Client:Terminating...\n")
            server.stop(0)
            nil
         }
      }


   private def client_2(
      x_2: Retry|Cancel|Success,
      c_Client_Svr_4: OutChannel[Withdraw1|Deposit1|Cancel],
      c_Client_Svr_3: InChannel[Retry|Success|Cancel]
   ):Client_2[x_2.type,c_Client_Svr_4.type,c_Client_Svr_3.type] =
      x_2 match {
         case x_2 : Retry => {
            displayMessage += s"Received Retry(message=${x_2.message})<br/>"
            print("Client:Actual type Received from x_2: Retry\n")
            print("Client:go to loop RecClient_1\n")
            loop(RecClient_1)
         }
         case x_2 : Cancel => {
            displayMessage += s"Received Cancel()<br/>"
            print("Client:Actual type Received from x_2: Cancel\n")
            print("Client:Terminating...\n")
            server.stop(0)
            nil
         }
         case x_2 : Success => {
            displayMessage += s"Received Success(message=${x_2.message})<br/>"
            print("Client:Actual type Received from x_2: Success\n")
            rec(RecClient_8){
               print("Client:entering loop RecClient_8\n")
               print("Client:Waiting selection Withdraw1|Deposit1|Cancel through channel c_Client_Svr_4\n")
               displayMessage += "Expecting to send Withdraw1(amount:Int) or Deposit1(amount:Int) or Cancel() through channel c_Client_Svr_4<br/>"
               possibleMessage = Map("Withdraw1" -> List("Int"),"Deposit1" -> List("Int"),"Cancel" -> List())
               canRelease = true
               semaphore.acquire
               if( choice == "Withdraw1"){
                  print("Client:Sending Withdraw1 through channel c_Client_Svr_4\n")
                  send(c_Client_Svr_4,Withdraw1(payloads(0).toInt)) >> {
                     receive(c_Client_Svr_3) {
                        (x_3:Retry|Success|Cancel) =>
                        displayMessage += s"Receiving type Retry|Success|Cancel through channel c_Client_Svr_3<br/>"
                        print("Client:Receive type Retry|Success|Cancel through channel c_Client_Svr_3\n")
                        client_3(x_3)
                     }
                  }
               }
               else if( choice == "Deposit1"){

                  print("Client:Sending Deposit1 through channel c_Client_Svr_4\n")
                  send(c_Client_Svr_4,Deposit1(payloads(0).toInt)) >> {
                     receive(c_Client_Svr_3) {
                        (x_4:Retry|Success|Cancel) =>
                        displayMessage += s"Receiving type Retry|Success|Cancel through channel c_Client_Svr_3<br/>"
                        print("Client:Receive type Retry|Success|Cancel through channel c_Client_Svr_3\n")
                        client_4(x_4)
                     }
                  }
               }
               else{
                  print("Client:Sending Cancel through channel c_Client_Svr_4\n")
                  send(c_Client_Svr_4,Cancel()) >> {
                     print("Client:Terminating...\n")
                     server.stop(0)
                     nil
                  }
               }
            }
         }
      }


   def client(
      c_Client_Svr_1: OutChannel[Connect],
      c_Client_Svr_2: OutChannel[Login],
      c_Client_Svr_4: OutChannel[Withdraw1|Deposit1|Cancel],
      c_Client_Svr_3: InChannel[Retry|Success|Cancel]
   ):Client[c_Client_Svr_1.type,c_Client_Svr_2.type,c_Client_Svr_4.type,c_Client_Svr_3.type] ={
      print("Client:Start running on host http://localhost:8080\n")
      server.createContext("/", new ClientRootHandler())
      server.start()
      print("Client:Waiting to send Connect through channel c_Client_Svr_1\n")
      displayMessage += "Expecting to send Connect() through channel c_Client_Svr_1<br/>"
      possibleMessage = Map("Connect" -> List())
      canRelease = true
      semaphore.acquire
      print("Client:Sending Connect through channel c_Client_Svr_1\n")
      send(c_Client_Svr_1,Connect()) >> {
         rec(RecClient_1){
            print("Client:entering loop RecClient_1\n")
            print("Client:Waiting to send Login through channel c_Client_Svr_2\n")
            displayMessage += "Expecting to send Login(userid:String,pw:String) through channel c_Client_Svr_2<br/>"
            possibleMessage = Map("Login" -> List("String","String"))
            canRelease = true
            semaphore.acquire
            print("Client:Sending Login through channel c_Client_Svr_2\n")
            send(c_Client_Svr_2,Login(payloads(0),payloads(1))) >> {
               receive(c_Client_Svr_3) {
                  (x_2:Retry|Cancel|Success) =>
                  displayMessage += s"Receiving type Retry|Cancel|Success through channel c_Client_Svr_3<br/>"
                  print("Client:Receive type Retry|Cancel|Success through channel c_Client_Svr_3\n")
                  client_2(x_2,c_Client_Svr_4,c_Client_Svr_3)
               }
            }
         }
      }
   }




class ClientRootHandler extends RootHandler("http://localhost:8080", "Client") {

    override def displayPayload(body: InputStream): Unit = {
      if (canRelease) {
        canRelease = false
        val bodyString = Source.fromInputStream(body).mkString
        val (isValid, label, payload, error) = checkString(bodyString, possibleMessage)
        errorMessage = error
        if (isValid) {
          displayMessage = ""
          choice = label
          payloads = payload
          semaphore.release()
        } else {
          canRelease = true
        }
      }
    }

    override def getDisplayMessage(): String = {
       displayMessage
    }

}