// Effpi - verified message-passing programs in Dotty
// Copyright 2020 Alceste Scalas
// Released under the MIT License: https://opensource.org/licenses/MIT

// This is the two-buyer protocol example from session types literature,
// implemented using match types
package effpi.examples.demo.threebuyersimple

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
              CQuote3 <: OutChannel[Quote | NotAvailable],
              CResp1 <: InChannel[Buy | Cancel],
              CResp2 <: InChannel[Buy | Cancel],
              CResp3 <: InChannel[Buy | Cancel],
              CConf1 <: OutChannel[Confirm | Cancel], 
              CConf2 <: OutChannel[Confirm | Cancel],
              CConf3 <: OutChannel[Confirm | Cancel]] =

    In[CTitle, String, String =>
        (Out[CQuote1, NotAvailable] >>: Out[CQuote2, NotAvailable] >>: Out[CQuote3, NotAvailable])
        |
        TrySell[CQuote1, CQuote2, CQuote3, CResp1, CResp2, CResp3, CConf1, CConf2, CConf3]
      ]

  type TrySell[CQuote1 <: OutChannel[Quote | NotAvailable],
                CQuote2 <: OutChannel[Quote | NotAvailable],
                CQuote3 <: OutChannel[Quote | NotAvailable],
                CResp1 <: InChannel[Buy | Cancel],
                CResp2 <: InChannel[Buy | Cancel],
                CResp3 <: InChannel[Buy | Cancel],
                CConf1 <: OutChannel[Confirm| Cancel], 
                CConf2 <: OutChannel[Confirm | Cancel],
                CConf3 <: OutChannel[Confirm | Cancel]]   = 

                  (Out[CQuote1, Quote] >>: Out[CQuote2, Quote] >>: Out[CQuote3, Quote] >>:
      In[CResp1, Buy|Cancel, (x: Buy|Cancel) => In[CResp2, Buy|Cancel, (y: Buy|Cancel) => In[CResp3, Buy|Cancel, (z: Buy|Cancel) => (Out[CConf1, Cancel] >>: Out[CConf2, Cancel] >>: Out[CConf3, Cancel]) |
         Out[CConf1, Confirm] >>:  Out[CConf2, Confirm] >>:  Out[CConf3, Confirm]]]])

  type PrimaryBuyer[CTitle <: OutChannel[String],
              CQuote <: InChannel[Quote | NotAvailable],
              CResp <: OutChannel[Buy|Cancel],
              CConf <: InChannel[Confirm | Cancel]] =
                   
                Out[CTitle, String] >>: In[CQuote, Quote|NotAvailable, (x: Quote|NotAvailable) => QuoteMatch[x.type, CResp, CConf]
    ]

    type SecondaryBuyer[CQuote <: InChannel[Quote | NotAvailable],
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
             cQuote3: OutChannel[Quote | NotAvailable],
             cResp1: InChannel[Buy | Cancel],
             cResp2: InChannel[Buy | Cancel],
             cResp3: InChannel[Buy | Cancel],
             cConf1: OutChannel[Confirm | Cancel],
             cConf2: OutChannel[Confirm | Cancel],
             cConf3: OutChannel[Confirm | Cancel]): Seller[cTitle.type, cQuote1.type, cQuote2.type, cQuote3.type, cResp1.type, cResp2.type, cResp3.type, cConf1.type, cConf2.type, cConf3.type] = {
    println("Seller: Starting Process")           
    receive(cTitle) {
      case "France" => sell(200, cQuote1, cQuote2, cQuote3, cResp1, cResp2, cResp3, cConf1, cConf2, cConf3)
      case "Germany" => sell(500, cQuote1, cQuote2, cQuote3, cResp1, cResp2, cResp3, cConf1, cConf2, cConf3)
      case "Belgium" => sell(700, cQuote1, cQuote2, cQuote3, cResp1, cResp2, cResp3, cConf1, cConf2, cConf3)
      case "Norway" => sell(1000, cQuote1, cQuote2, cQuote3, cResp1, cResp2, cResp3, cConf1, cConf2, cConf3)
      case _ => {
        println("Seller: Location doesnt exist, sending Not available to Alice, Bob and Carol")
        send(cQuote1, NotAvailable()) >> {send(cQuote2, NotAvailable()) >> {send(cQuote3, NotAvailable())}}
        }
    }
  }

  def sell(amount: Int, cQuote1: OutChannel[Quote|NotAvailable], cQuote2: OutChannel[Quote|NotAvailable], cQuote3: OutChannel[Quote|NotAvailable],
           cResp1: InChannel[Buy | Cancel], cResp2: InChannel[Buy | Cancel], cResp3: InChannel[Buy | Cancel], cConf1: OutChannel[Confirm | Cancel], cConf2: OutChannel[Confirm | Cancel], 
           cConf3: OutChannel[Confirm | Cancel])
            : TrySell[cQuote1.type, cQuote2.type, cQuote3.type, cResp1.type, cResp2.type, cResp3.type, cConf1.type, cConf2.type, cConf3.type] = {
    println(s"Seller: Sending quote ${amount} to Alice")
    send(cQuote1, Quote(amount)) >> {
      println(s"Seller: Sending quote ${amount} to Bob")
      send(cQuote2, Quote(amount)) >> {
        println(s"Seller: Sending quote ${amount} to Carol")
        send(cQuote3, Quote(amount)) >> 
        receive(cResp1) { (res1: Buy|Cancel) => {
          println("Seller: Received response from Alice")
          receive(cResp2) {
            println("Seller: Received response from Bob")
            (res2: Buy | Cancel) => 
               {
                receive(cResp3){
                     println("Seller: Received response from Carol")
                     (res3: Buy |Cancel) =>
                        (res1, res2, res3) match {
                        case(res1: Buy, res2: Buy, res3: Buy) => {
                          println(s"Seller: Received Buy from all, sending booking date to address ${res1.address} and ${res2.address}")
                          val date = LocalDate.now().plusWeeks(1)
                          send(cConf1, Confirm(date)) >> {send(cConf2, Confirm(date)) >> {send(cConf3, Confirm(date))}}
                          }
                        case _ => {
                          println("Seller: Received Cancellation from one or more, sending cancel to all")
                          send(cConf1, Cancel()) >> {send(cConf2, Cancel())>> {send(cConf3, Cancel())}}
                          }
                      }
                    }
                }
                }
        } 
        }
    }
    }
  }


  def alice(dest: String,
            cTitle: OutChannel[String],
            cQuote: InChannel[Quote | NotAvailable],
            cResp: OutChannel[Buy | Cancel],
            cConf: InChannel[Confirm | Cancel]): PrimaryBuyer[cTitle.type, cQuote.type, cResp.type, cConf.type] = {
    println("Alice : Starting Process")
    println(s"Alice : Sending request for ${dest}")
    send(cTitle, dest) >> {
      receive(cQuote) { (q: Quote|NotAvailable) => {
        handleQuote("Alice ",900,"Baker Street",q, cResp, cConf)
      }
      }
    }
  }

  def bob(cQuote: InChannel[Quote | NotAvailable],
      cResp: OutChannel[Buy | Cancel],
      cConf: InChannel[Confirm | Cancel]): SecondaryBuyer[cQuote.type, cResp.type, cConf.type] = {
          println("Bob   : Starting Process")
                                      receive(cQuote) { (q: Quote|NotAvailable) =>
                                        handleQuote("Bob   ", 600 , "Exhibition Road",q, cResp, cConf)
                                      }
                                      } 

  def carol(cQuote: InChannel[Quote | NotAvailable],
  cResp: OutChannel[Buy | Cancel],
  cConf: InChannel[Confirm | Cancel]): SecondaryBuyer[cQuote.type, cResp.type, cConf.type] = {
      println("Carol : Starting Process")
                                  receive(cQuote) { (q: Quote|NotAvailable) =>
                                    handleQuote("Carol ", 300 , "South Kensington",q, cResp, cConf)
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

    val destinations = Array("Switzerland", "France","Germany", "Belgium", "Norway")
    for(dest <- destinations){
      val (ctitle, cquote1, cquote2, cquote3, cresp1, cresp2, cresp3, cconf1, cconf2, cconf3) = (Channel[String](), Channel[Quote | NotAvailable](),
      Channel[Quote | NotAvailable](),Channel[Quote | NotAvailable](), Channel[Buy | Cancel](), Channel[Buy | Cancel](), Channel[Buy | Cancel](), Channel[Confirm| Cancel](), Channel[Confirm| Cancel](), Channel[Confirm| Cancel]())
      
      eval(par(seller(ctitle, cquote1, cquote2, cquote3, cresp1, cresp2, cresp3, cconf1, cconf2, cconf3), 
      alice(dest, ctitle, cquote1, cresp1, cconf1), bob(cquote2, cresp2, cconf2),
      carol(cquote3, cresp3, cconf3)))
      println()
      Thread.sleep(1000)
    }

    Thread.sleep(5000) // Wait 5 seconds
    ps.kill()
  }
}
