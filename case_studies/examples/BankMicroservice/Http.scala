package sandbox.Http

import java.io.{InputStream, OutputStream}
import java.net.URLDecoder
import scala.language.postfixOps
import java.io.InputStream
import java.nio.charset.StandardCharsets
import com.sun.net.httpserver.{HttpExchange, HttpHandler}

abstract class RootHandler(host: String, role:String) extends HttpHandler {

  var errorMessage = ""
  var display = ""

  def handle(t: HttpExchange) = {
    errorMessage = ""
    displayPayload(t.getRequestBody)
    display = getDisplayMessage()
    sendResponse(t)
  }

  protected def displayPayload(body: InputStream): Unit

  protected def getDisplayMessage(): String

  private def copyStream(in: InputStream, out: OutputStream) = {
    Iterator
      .continually(in.read)
      .takeWhile(-1 !=)
      .foreach(out.write)
  }

  protected def checkString(input: String, possibleMessage: Map[String, List[String]]) = {
    val splitArray = input.split("&")
    if (splitArray.size == 2) {
      if (splitArray(0).split("=").size == 2 && splitArray(1).split("=").size <= 2) {
        val label = splitArray(0).split("=").toList(1)
        if (possibleMessage.contains(label)) {
          var payload = Array[String]()
          if (splitArray(1).split("=").size == 2 && splitArray(1).split("=")(1) != "") {
              payload = splitArray(1).split("=")(1).split("%2C")
          }
          val messageTypes = possibleMessage.get(label).get
          if (payload.size == messageTypes.size) {
            var checkPayload = true
            var i = 0
            for (i <- 0 until payload.size) {
              if (messageTypes(i) == "Int" && (!payload(i).forall(_.isDigit) || payload(i) == "")) {
                checkPayload = false
              } else if (messageTypes(i) == "String") {
                payload(i) = URLDecoder.decode(payload(i), "UTF-8")
              }
            }
            if (checkPayload) {
              (true, label, payload.toList, "")
            } else {
              (false, "", List[String](), "Error: payload types don't match")
            }
          } else {
            (false, "", List[String](), "Error: Incorrect number of payloads for this labels (',' in string is not supported at the moment)")
          }
        } else {
          (false, "", List[String](), "Error: Not a valid case class")
        }
      } else if (splitArray(0).split("=").size < 2) {
        (false, "", List[String](), "")
      } else {
        (false, "", List[String](), "Error: REPORT THIS ERROR")
      }

    } else if (splitArray.size > 2) {
      (false, "", List[String](), "Error: REPORT THIS ERROR.")
    } else {
      (false, "", List[String](), "")
    }
  }

  private def sendResponse(t: HttpExchange) = {
    val response = "<html>\n"
    + "<body>\n"
    + "<h1>" + role + "</h1>\n\n"
    + display
    + "\n"
    + "<form action=\"" + host + "\" method=\"post\">\n"
    + "Case Class: <input type=\"text\" name=\"caseclass\"><br>\n"
    + "Payloads: <input type=\"text\" name=\"payloads\"><br>\n"
    + "<input type=\"submit\">\n"
    + "</form>\n"
    + "\n"
    + errorMessage
    + "</body>\n"
    + "</html> ";
    t.sendResponseHeaders(200, response.length())
    val os = t.getResponseBody
    os.write(response.getBytes)
    os.close()
  }
}
