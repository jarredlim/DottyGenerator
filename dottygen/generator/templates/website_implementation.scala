package effpi_sandbox.ROLE.implementation

import effpi_sandbox.caseclass._
import effpi_sandbox.ROLE.types._
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
import effpi_sandbox.Http.RootHandler

implicit val timeout: Duration = Duration("TIMEOUT seconds")
private val semaphore = new Semaphore(0)

private var canRelease = false
private var possibleMessage = Map[String, List[String]]()
private var choice = ""
private var displayMessage = ""
private var payloads = List[String]()
private var server = HttpServer.create(new InetSocketAddress(HOST), 0)

IMPLEMENTATIONS

class ROLERootHandler extends RootHandler("http://localhost:HOST", "ROLE") {

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