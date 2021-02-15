// Effpi - verified message-passing programs in Dotty
// Copyright 2020 Alceste Scalas
// Released under the MIT License: https://opensource.org/licenses/MIT

// This is the two-buyer protocol example from session types literature,
// implemented using match types
package effpi.examples.demo.twobuyerdirect

import scala.concurrent.duration.Duration

import effpi.process._
import effpi.process.dsl._
import effpi.channel.{Channel, InChannel, OutChannel}
import java.time.LocalDate

package types {

  import effpi.process.dsl._
  implicit val timeout: Duration = Duration("30 seconds")

  trait HasRetTy {
  type RetTy
  def ret[ArgTy](x : ArgTy) : RetTy
  }

    sealed abstract class BobResp extends HasRetTy {
      type RetTy <: Process
    }

  //Message Classes
  case class Quote(amount: Int)
  case class NotAvailable()

  case class Contrib(amount: Int)
  case class Cancel() extends BobResp {
    type RetTy = Out[OutChannel[Buy|Cancel], Cancel]
    type ArgTy = (OutChannel[Buy|Cancel])
    override def ret[ArgTy](x : ArgTy) : RetTy = {
      println("Alice : Reveived Cancel from Bob, send cancel to seller.")
      send(x.asInstanceOf[OutChannel[Buy|Cancel]], Cancel())
    }
  }

  case class OK() extends BobResp {
    type RetTy = Out[OutChannel[Buy|Cancel], Buy] >>: In[InChannel[Confirm], Confirm, Confirm => PNil]
    type ArgTy = (OutChannel[Buy|Cancel], InChannel[Confirm])
    override def ret[ArgTy](x : ArgTy) : RetTy = x match{
         case(send_chan, recv_chan) => {
          println("Alice : Reveived Ok from Bob, send buy to seller.")
          send(send_chan.asInstanceOf[OutChannel[Buy|Cancel]], Buy("Oxford Street")) >> receive(recv_chan.asInstanceOf[InChannel[Confirm]]) { (conf: Confirm) => {
            println(s"Alice : Purchase confirmed, Booking date is ${conf.date}")
            nil
          }
        }
         }
    }
  }

  case class Buy(address: String)
  case class Confirm(date: LocalDate)

  type Seller[CTitle <: InChannel[String],
              CQuote1 <: OutChannel[Quote | NotAvailable],
              CQuote2 <: OutChannel[Quote | NotAvailable],
              CResp <: InChannel[Buy | Cancel],
              CConf <: OutChannel[Confirm]] =

              In[CTitle, String, String =>
              (Out[CQuote1, NotAvailable] >>: Out[CQuote2, NotAvailable]) |
               TrySell[CQuote1, CQuote2, CResp, CConf]]

  type TrySell[CQuote1 <: OutChannel[Quote | NotAvailable],
                CQuote2 <: OutChannel[Quote | NotAvailable],
                CResp <: InChannel[Buy | Cancel],
                CConf <: OutChannel[Confirm]] =

               Out[CQuote1, Quote] >>:  Out[CQuote2, Quote] >>: In[CResp, Buy | Cancel, (x: Buy | Cancel) => SellerMatch[x.type, CConf]]

  type SellerMatch[X <: Buy|Cancel, CConf <: OutChannel[Confirm]] <: Process = X match {
         case Buy => Out[CConf, Confirm]
          case Cancel => PNil
}

  type Alice[CTitle <: OutChannel[String],
            CQuote <: InChannel[Quote | NotAvailable],
            CBobContrib <: OutChannel[Contrib|Cancel],
            CBobResp <: InChannel[BobResp],
            CResp <: OutChannel[Buy | Cancel],
            CConf <: InChannel[Confirm]] =
        Out[CTitle, String] >>: In[CQuote, Quote| NotAvailable, (x: Quote|NotAvailable) =>
            QuoteMatch[x.type,CBobContrib,CBobResp,CResp, CConf]]

   type QuoteMatch[X <: Quote|NotAvailable,
                    CBobContrib <: OutChannel[Contrib|Cancel],
                    CBobResp <: InChannel[BobResp],
                    CResp <: OutChannel[Buy | Cancel],
                    CConf <: InChannel[Confirm]] <: Process = X match {
  case NotAvailable => Out[CBobContrib, Cancel]
  case Quote => (Out[CBobContrib, Cancel] >>: Out[CResp, Cancel]) |
                (Out[CBobContrib, Contrib] >>:
                In[CBobResp, BobResp, (x: BobResp) =>
                  // BobRespMatch[x.type, CResp, CConf]
                  x.RetTy
                ])
                    }

  //This will cause nested matching Type in Alice
  // type BobRespMatch[Y <: OK|Cancel,
  //                   CResp <: OutChannel[Buy|Cancel],
  //                   CConf <: InChannel[Confirm]] <: Process = Y match {
  //                case Cancel => Out[CResp, Cancel]
  //                case OK => Out[CResp, Buy] >>: In[CConf, Confirm, Confirm => PNil]
  // }

  //Relaxed type
  // type BobRespMatch[Y <: OK|Cancel,
  //                   CResp <: OutChannel[Buy|Cancel],
  //                   CConf <: InChannel[Confirm]] = Out[CResp, Cancel] | Out[CResp, Buy] >>: In[CConf, Confirm, Confirm => PNil]

  type Bob[CQuote <: InChannel[Quote | NotAvailable],
            CBobContrib <: InChannel[Contrib|Cancel],
             CBobResp <: OutChannel[BobResp]] =  In[CQuote, Quote| NotAvailable, (x: Quote|NotAvailable) =>
              In[CBobContrib, Contrib|Cancel, (y: Contrib|Cancel) =>  BobQuoteMatch[x.type, y.type, CBobResp]]]

  type BobQuoteMatch[X<: Quote|NotAvailable, Y <: Contrib | Cancel, CBobResp <: OutChannel[BobResp]] =
                           (Out[CBobResp, OK] | Out[CBobResp,  Cancel]) | PNil

}

package implementation {

  import types._
  implicit val timeout: Duration = Duration("30 seconds")
  import effpi.process.dsl._

  def bob(cQuote: InChannel[Quote | NotAvailable],
          cBobContrib: InChannel[Contrib|Cancel],
          cBobResp: OutChannel[BobResp]): Bob[cQuote.type, cBobContrib.type, cBobResp.type] = {
            println("Bob   : starting process")
            receive(cQuote){
              (q: Quote|NotAvailable) => receive(cBobContrib){
                (r: Contrib | Cancel) => {(q,r) match{
                  case (q:Quote, r:Contrib) => {
                    println(s"Bob   : received Quote ${q.amount}  from seller and contribution ${r.amount} from Alice. ")
                     if(r.amount < q.amount/2){
                        println(s"Bob   : Alice is paying ${r.amount}, less than half of ${q.amount}, sending cancel to alice")
                        send(cBobResp, Cancel())
                     }else{
                      println(s"Bob   : Alice is paying ${r.amount}, more than half of ${q.amount}, sending ok to alice")
                      send(cBobResp, OK())
                     }
                  }
                  case (q: NotAvailable, r: Cancel) => {
                    println("Bob : received Not Available info from Seller and Cancel from Alice, terminating")
                    nil
                  }
                  case _ => {
                    println("Bob : received Cancel from Alice, terminating")
                    nil
                  }
                }
              }
              }
            }
          }

  def alice (title: String,
             payable: Int,
             cTitle: OutChannel[String],
             cQuote: InChannel[Quote | NotAvailable],
             cBobContrib: OutChannel[Contrib|Cancel],
            cBobResp: InChannel[BobResp],
             cResp: OutChannel[Buy | Cancel],
              cConf: InChannel[Confirm]
  ): Alice[cTitle.type, cQuote.type,cBobContrib.type, cBobResp.type, cResp.type, cConf.type] = {
     println("Alice : starting process")
     println(s"Alice : sending request to $title")
       send(cTitle, title) >> {
             receive(cQuote){
                (q: Quote|NotAvailable) => q match {
                   case _:NotAvailable => {
  println("Alice : received Not Available info, inform bob it's cancelled")
  send(cBobContrib, Cancel())
}

        case q:Quote => {
          if(q.amount > 1000){
              println(s"Alice : ${q.amount} exceeds 1000. Too expensive , send cancel to Bob and seller")
              send(cBobContrib, Cancel()) >> send(cResp, Cancel())}
           else{
            println(s"Alice : ${q.amount} less than 1000. Acceptable , send ${payable} to bob")
            send(cBobContrib, Contrib(payable)) >> {
              receive(cBobResp){  (res: BobResp) => res match {
                case _ : Cancel => res.ret(cResp)
                case _ : OK => res.ret((cResp, cConf))
              }
              }
            }
        }
        }
}
}
}
}

  // def handleRes(res : BobResp,  
  //               cResp: OutChannel[Buy | Cancel], 
  //               cConf: InChannel[Confirm]) 
  //            : BobRespMatch[res.type, cResp.type,  cConf.type] = {res match {
  //             case _ : Cancel => res.ret(cResp)
  //               // {
  //               // println("Alice : Reveived Cancel from Bob, send cancel to seller.")
  //               // send(cResp, Cancel())
  //               // }
  //             case _ : OK => res.ret(cResp, cConf)
  //             //   {
  //             //   println("Alice : Reveived Ok from Bob, send buy to seller.")
  //             //           send(cResp, Buy("Oxford Street")) >> receive(cConf) { (conf: Confirm) => {
  //             //             println(s"Alice : Purchase confirmed, Booking date is ${conf.date}")
  //             //             nil
  //             //           }
  //             //         }
  //             // }
  //         }
  //            }

  def seller(cTitle: InChannel[String],
              cQuote1: OutChannel[Quote | NotAvailable],
              cQuote2: OutChannel[Quote | NotAvailable],
              cResp: InChannel[Buy | Cancel],
              cConf: OutChannel[Confirm]): Seller[cTitle.type, cQuote1.type, cQuote2.type, cResp.type, cConf.type]=
  {
    println("Seller: Starting Process")
          receive(cTitle){
              case "Las Vegas" => sell(900, cQuote1, cQuote2, cResp, cConf)
              case "New York" => sell(1060, cQuote1, cQuote2, cResp, cConf)
              case "California" => sell(800, cQuote1, cQuote2, cResp, cConf)
              case _ => {
               println("Seller: Location Doesn't Exist, informing both clients")
               send(cQuote1, NotAvailable()) >> send(cQuote2, NotAvailable()) }
            }
      }

  def sell(amount : Int, cQuote1: OutChannel[Quote | NotAvailable], cQuote2: OutChannel[Quote | NotAvailable], cResp: InChannel[Buy | Cancel],
cConf: OutChannel[Confirm]): TrySell[cQuote1.type,cQuote2.type, cResp.type, cConf.type] = {
      var  currAmount = amount
      println(s"Seller: Received Order, Sending Quote value ${currAmount}")
      send(cQuote1, Quote(currAmount)) >> {send(cQuote2, Quote(currAmount)) >> {
              receive(cResp) { (res: Buy|Cancel) => res match {
              case res : Buy => {
                   println(s"Seller: Order received. Shipping to ${res.address}, sending booking date")
                        send(cConf, Confirm(LocalDate.now().plusWeeks(1)))
                    }
                    case _: Cancel => {
                      println("Seller: Received Cancelation")
                      nil
              }
                    } }
                    }
}
}
}



// To run this example, try:
// sbt "examples/runMain effpi.examples.demo.twobuyerdirect.Main"
object Main {
  import types._
  import implementation._
  def main(): Unit = main(Array())
  
  def main(args: Array[String]) = {
    println("Running demo...")
    println()
    implicit val ps = effpi.system.ProcessSystemRunnerImproved()
    val destinations = Array(("Las Vegas", 200), ("New York", 700), ("California", 
    500), ("Massachuettes", 500))
    for( (dest, payable) <- destinations){
      val (ctitle, cquote1, cquote2, cbobcontrib, cbobresp, cresp, cconf) = (Channel[String](), Channel[Quote | NotAvailable](), Channel[Quote | NotAvailable](),
        Channel[Contrib | Cancel](), Channel[BobResp](), Channel[Buy | Cancel](), Channel[Confirm]())
      
      eval(par(seller(ctitle, cquote1, cquote2, cresp, cconf), alice(dest, payable, ctitle, cquote1, cbobcontrib, cbobresp, cresp, cconf),
      bob(cquote2, cbobcontrib, cbobresp)))
      Thread.sleep(1000)
      println()
    }
    ps.kill()
  }
}
