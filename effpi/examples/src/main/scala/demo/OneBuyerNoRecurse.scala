// Effpi - verified message-passing programs in Dotty
// Copyright 2020 Alceste Scalas
// Released under the MIT License: https://opensource.org/licenses/MIT

// This is the two-buyer protocol example from session types literature,
// implemented using match types
package effpi.examples.demo.onebuyer

import scala.concurrent.duration.Duration

import effpi.process._
import effpi.process.dsl._
import effpi.channel.{Channel, InChannel, OutChannel}
import java.time.LocalDate

package types {

  //Message Classes
  case class Quote(amount: Int)
  case class NotAvailable()

  case class Cancel()
  case class Buy(address: String)
  case class Confirm(date: LocalDate)

  type Seller[CTitle <: InChannel[String],
              CQuote <: OutChannel[Quote | NotAvailable],
              CResp <: InChannel[Buy | Cancel],
              CConf <: OutChannel[Confirm]] =

              In[CTitle, String, String =>
              Out[CQuote, NotAvailable] |
               TrySell[CQuote, CResp, CConf]]

  type TrySell[CQuote <: OutChannel[Quote | NotAvailable],
                CResp <: InChannel[Buy | Cancel],
                CConf <: OutChannel[Confirm]] =

               Out[CQuote, Quote] >>: In[CResp, Buy | Cancel, (x: Buy | Cancel) => SellerMatch[x.type, CConf]]

  type SellerMatch[X <: Buy|Cancel, CConf <: OutChannel[Confirm]] <: Process = X match {
         case Buy => Out[CConf, Confirm]
          case Cancel => PNil
}

  type Alice[CTitle <: OutChannel[String],
            CQuote <: InChannel[Quote | NotAvailable],
            CResp <: OutChannel[Buy | Cancel],
            CConf <: InChannel[Confirm]] =
        Out[CTitle, String] >>: In[CQuote, Quote| NotAvailable, (x: Quote|NotAvailable) =>
            QuoteMatch[x.type,CResp, CConf]]

   type QuoteMatch[X <: Quote|NotAvailable,
                    CResp <: OutChannel[Buy | Cancel],
                    CConf <: InChannel[Confirm]] <: Process = X match {
  case NotAvailable => PNil
  case Quote => ( Out[CResp, Buy] >>: In[CConf, Confirm, Confirm => PNil] ) |
                 Out[CResp, Cancel]
}
}

package implementation {

  import types._
  implicit val timeout: Duration = Duration("30 seconds")
  import effpi.process.dsl._

  def alice (title: String,
             cTitle: OutChannel[String],
             cQuote: InChannel[Quote | NotAvailable],
             cResp: OutChannel[Buy | Cancel],
              cConf: InChannel[Confirm]
  ): Alice[cTitle.type, cQuote.type, cResp.type, cConf.type] = {
     println("Alice : starting process")
     println(s"Alice : sending request to $title")
       send(cTitle, title) >> {
             receive(cQuote){
                (q: Quote|NotAvailable) => q match {
                   case _:NotAvailable => {
  println("Alice : received Not Available info")
  nil
}
                   
case q:Quote => {if(q.amount > 1000){
println("Alice : Too expensive, send cancel")
send(cResp, Cancel())} else{
println("Alice : Affordable, send address and buy")
send(cResp, Buy("Oxford Street")) >> receive(cConf) { (conf: Confirm) =>
println(s"Alice : Purchase confirmed, Booking date is ${conf.date}")
nil
}
}}
}
}
}
}
  
  def seller(cTitle: InChannel[String],
              cQuote: OutChannel[Quote | NotAvailable],
              cResp: InChannel[Buy | Cancel],
              cConf: OutChannel[Confirm]): Seller[cTitle.type, cQuote.type, cResp.type, cConf.type]=
  {
    println("Seller: Starting Process")
          receive(cTitle){
              case "Las Vegas" => sell(900, cQuote, cResp, cConf)
              case "New York" => sell(1060, cQuote, cResp, cConf)
              case _ => {
               println("Seller: Location Doesn't Exist, informing client")
               send(cQuote, NotAvailable())}
            }
      }
  
  def sell(amount : Int, cQuote: OutChannel[Quote | NotAvailable], cResp: InChannel[Buy | Cancel],
cConf: OutChannel[Confirm]): TrySell[cQuote.type, cResp.type, cConf.type] = {
  println(s"Seller: Received Order, Sending Quote Value ${amount}")
    send(cQuote, Quote(amount)) >> {
          receive(cResp) { (res: Buy|Cancel) => res match {
          case res: Buy => {
               println(s"Seller: Order received. Shipping to ${res.address}, sending booking date")
                    send(cConf, Confirm(LocalDate.now().plusWeeks(1)))
                }
                case _: Cancel => {
                  println("Seller : Received Cancelation")
                  nil
}
                } }
                }
}
}

// To run this example, try:
// sbt "examples/runMain effpi.examples.demo.onebuyer.Main"
object Main {
  import types._
  import implementation._
  def main(): Unit = main(Array())

  def main(args: Array[String]) = {
    println("Running demo...")
    println()
    implicit val ps = effpi.system.ProcessSystemRunnerImproved()
    val destinations = Array("Las Vegas","New York", "Hawaii")
    for(dest <- destinations){
      val (ctitle, cquote, cresp, cconf) = (Channel[String](), Channel[Quote | NotAvailable](),
        Channel[Buy | Cancel](), Channel[Confirm]())
  
      eval(par(seller(ctitle, cquote, cresp, cconf), alice(dest, ctitle, cquote, cresp, cconf)))
      println()
      Thread.sleep(1000)
    }
    ps.kill()
  }
}
