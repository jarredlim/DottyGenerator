// Effpi - verified message-passing programs in Dotty
// Copyright 2020 Alceste Scalas
// Released under the MIT License: https://opensource.org/licenses/MIT

// This is the two-buyer protocol example from session types literature,
// implemented using match types
package effpi.examples.demo.twobuyersimple

import scala.concurrent.duration.Duration

import effpi.process._
import effpi.process.dsl._
import effpi.channel.{Channel, InChannel, OutChannel}
import java.time.LocalDate

package types {
  import effpi.process.dsl._

  // Message classes
  case class Quote(amount: Int)
  case class NotAvailable()

  case class Cancel()

  case class Buy(address: String)
  case class Confirm(delivery: LocalDate)
  
  type Seller[CTitle <: InChannel[String],
              CQuote1 <: OutChannel[Quote | NotAvailable],
              CQuote2 <: OutChannel[Quote | NotAvailable],
              CResp1 <: InChannel[Buy | Cancel],
              CResp2 <: InChannel[Buy | Cancel],
              CConf1 <: OutChannel[Confirm | Cancel], 
              CConf2 <: OutChannel[Confirm | Cancel]] =

    In[CTitle, String, String =>
        (Out[CQuote1, NotAvailable] >>: Out[CQuote2, NotAvailable])
        |
        TrySell[CQuote1, CQuote2, CResp1, CResp2, CConf1, CConf2]
      ]

  type TrySell[CQuote1 <: OutChannel[Quote | NotAvailable],
                CQuote2 <: OutChannel[Quote | NotAvailable],
                CResp1 <: InChannel[Buy | Cancel],
                CResp2 <: InChannel[Buy | Cancel],
                CConf1 <: OutChannel[Confirm| Cancel], 
                CConf2 <: OutChannel[Confirm | Cancel]]   = 

                  (Out[CQuote1, Quote] >>: Out[CQuote2, Quote] >>:
      In[CResp1, Buy|Cancel, (x: Buy|Cancel) => In[CResp2, Buy|Cancel, (y: Buy|Cancel) => (Out[CConf1, Cancel] >>: Out[CConf2, Cancel]) |
         Out[CConf1, Confirm] >>:  Out[CConf2, Confirm]]])

  type Alice[CTitle <: OutChannel[String],
              CQuote <: InChannel[Quote | NotAvailable],
              CResp <: OutChannel[Buy|Cancel],
              CConf <: InChannel[Confirm | Cancel]] =
                   
                Out[CTitle, String] >>: In[CQuote, Quote|NotAvailable, (x: Quote|NotAvailable) => QuoteMatch[x.type, CResp, CConf]
    ]

    type Bob[CQuote <: InChannel[Quote | NotAvailable],
              CResp <: OutChannel[Buy|Cancel],
              CConf <: InChannel[Confirm | Cancel]] =
                   
                In[CQuote, Quote|NotAvailable, (x: Quote|NotAvailable) => QuoteMatch[x.type, CResp, CConf]
    ]

    type QuoteMatch[X <: Quote| NotAvailable,
                    CResp <: OutChannel[Buy | Cancel],
                    CConf <: InChannel[Confirm | Cancel]] <: Process = X match {
                       case NotAvailable => PNil
                       case Quote => (Out[CResp, Cancel] >>: In[CConf,Confirm| Cancel, (x: Confirm | Cancel) =>  PNil]) |
                                     (Out[CResp, Buy] >>: In[CConf,Confirm| Cancel, (x: Confirm | Cancel) =>  PNil])
                    }


  
}

package implementation {
  import types._
  implicit val timeout: Duration = Duration("30 seconds")
  import effpi.process.dsl._


  def seller(cTitle: InChannel[String],
             cQuote1: OutChannel[Quote | NotAvailable],
             cQuote2: OutChannel[Quote | NotAvailable],
             cResp1: InChannel[Buy | Cancel],
             cResp2: InChannel[Buy | Cancel],
             cConf1: OutChannel[Confirm | Cancel],
             cConf2: OutChannel[Confirm | Cancel]): Seller[cTitle.type, cQuote1.type, cQuote2.type, cResp1.type, cResp2.type, cConf1.type, cConf2.type] = {
    println("Seller: Starting Process")           
    receive(cTitle) {
      case "France" => sell(300, cQuote1, cQuote2, cResp1, cResp2, cConf1, cConf2)
      case "Germany" => sell(700, cQuote1, cQuote2, cResp1, cResp2, cConf1, cConf2)
      case "Belgium" => sell(1100, cQuote1, cQuote2, cResp1, cResp2, cConf1, cConf2)
      case _ => {
        println("Seller: Location doesnt exist, sending Not available to Alice and Bob")
        send(cQuote1, NotAvailable()) >> send(cQuote2, NotAvailable())
        }
    }
  }

  def sell(amount: Int, cQuote1: OutChannel[Quote|NotAvailable], cQuote2: OutChannel[Quote|NotAvailable],
           cResp1: InChannel[Buy | Cancel], cResp2: InChannel[Buy | Cancel], cConf1: OutChannel[Confirm | Cancel], cConf2: OutChannel[Confirm | Cancel])
            : TrySell[cQuote1.type, cQuote2.type, cResp1.type, cResp2.type, cConf1.type, cConf2.type] = {
    println(s"Seller: Sending quote ${amount} to Alice")
    send(cQuote1, Quote(amount)) >> {
      println(s"Seller: Sending quote ${amount} to Bob")
      send(cQuote2, Quote(amount)) >>
      receive(cResp1) { (res1: Buy|Cancel) => {
        println("Seller: Received response from Alice")
        receive(cResp2) {
          println("Seller: Received response from Bob")
          (res2: Buy | Cancel) => (res1, res2) match {
             case(res1: Buy, res2: Buy) => {
              println(s"Seller: Received Buy from both, sending booking date to address ${res1.address} and ${res2.address}")
              val date = LocalDate.now().plusWeeks(1)
               send(cConf1, Confirm(date)) >> send(cConf2, Confirm(date))
               }
             case _ => {
              println("Seller: Received Cancellation from one or more, sending cancel to both")
               send(cConf1, Cancel()) >> send(cConf2, Cancel())
               }
          }
        }
      } }
    }
  }


  def alice(dest: String,
            cTitle: OutChannel[String],
            cQuote: InChannel[Quote | NotAvailable],
            cResp: OutChannel[Buy | Cancel],
            cConf: InChannel[Confirm | Cancel]): Alice[cTitle.type, cQuote.type, cResp.type, cConf.type] = {
    println("Alice : Starting Process")
    println(s"Alice : Sending request for ${dest}")
    send(cTitle, dest) >> {
      receive(cQuote) { (q: Quote|NotAvailable) => {
        handleQuote("Alice ",1000,"Baker Street",q, cResp, cConf)
      }
      }
    }
  }

  def bob(cQuote: InChannel[Quote | NotAvailable],
      cResp: OutChannel[Buy | Cancel],
      cConf: InChannel[Confirm | Cancel]): Bob[cQuote.type, cResp.type, cConf.type] = {
          println("Bob   : Starting Process")
                                      receive(cQuote) { (q: Quote|NotAvailable) =>
                                        handleQuote("Bob   ", 500 , "Exhibition Road",q, cResp, cConf)
                                      }
                                      } 

  def handleQuote(user: String,
                  budget: Int,
                  address: String,
                  q: Quote|NotAvailable,
                  cResp: OutChannel[Buy | Cancel],
                  cConf: InChannel[Confirm | Cancel]): QuoteMatch[q.type,cResp.type, cConf.type] = q match {
        case _: NotAvailable => {
          println(s"${user}: Received Not Available Info, Terminating.....")
          nil
        }
        case q: Quote => {
          if (q.amount > budget) {
            println(s"${user}: Quote ${q.amount} exceeds budget ${budget}, sending cancel to seller")
               send(cResp, Cancel())  >> {
                 receive(cConf){(res: Confirm|Cancel) => handleRes(user, res)}
               }
          } else {
            println(s"${user}: Quote ${q.amount} less than budget ${budget}, sending confirmation to seller")
            send(cResp, Buy(address)) >> {
              receive(cConf){(res: Confirm|Cancel) => handleRes(user,res)}
            }
          }
        }
      }

   def handleRes(user: String,res: Confirm| Cancel) : PNil = res match {
       case _ : Cancel => {
        println(s"${user}: Received Cancellation from Seller, Terminating...")
        nil
       }
       case res: Confirm => {
        println(s"${user}: Received Confirmation from Seller, booking date is ${res.delivery}")
         nil
       }
   }   

}

// To run this example, try:
// sbt "examples/runMain effpi.examples.demo.twobuyersimple.Main"
object Main {
  import types._
  import implementation._

  def main(): Unit = main(Array())

  def main(args: Array[String]) = {
    println("Running demo...")
    println()
    implicit val ps = effpi.system.ProcessSystemRunnerImproved()

    val destinations = Array("Switzerland", "France","Germany", "Belgium")
    for(dest <- destinations){
      val (ctitle, cquote1, cquote2, cresp1, cresp2, cconf1, cconf2) = (Channel[String](), Channel[Quote | NotAvailable](),
      Channel[Quote | NotAvailable](), Channel[Buy | Cancel](), Channel[Buy | Cancel](), Channel[Confirm| Cancel](), Channel[Confirm| Cancel]())
      
      eval(par(seller(ctitle, cquote1, cquote2, cresp1, cresp2, cconf1, cconf2), 
      alice(dest, ctitle, cquote1, cresp1, cconf1), bob(cquote2, cresp2, cconf2)))
      println()
      Thread.sleep(1000)
    }

    Thread.sleep(5000) // Wait 5 seconds
    ps.kill()
  }
}
