// Effpi - verified message-passing programs in Dotty
// Copyright 2020 Alceste Scalas
// Released under the MIT License: https://opensource.org/licenses/MIT

// This is the two-buyer protocol example from session types literature,
// implemented using match types
package effpi.examples.demo.twobuyer

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

  case class Quote(amount: Int)
  case class NotAvailable()

  case class Contrib(amount: Int)
  case class Cancel() extends BobResp {
    type RetTy = Out[OutChannel[Buy|Cancel], Cancel]
    type ArgTy = OutChannel[Buy|Cancel]
    def ret[ArgTy](x : ArgTy) : RetTy = {
      println(s"Alice: Received Cancel from Bob, sending Cancel to Seller")
      send(x.asInstanceOf[OutChannel[Buy|Cancel]], Cancel())
    }
  }

  case class OK() extends BobResp {
     type RetTy = Out[OutChannel[Buy|Cancel], Buy] >>: In[InChannel[Confirm], Confirm, Confirm => PNil]
     type ArgTy = ( OutChannel[Buy|Cancel] , InChannel[Confirm])
     def ret[ArgTy](x:ArgTy) : RetTy = x match{
      case(cResp, cConf) => {
        println(s"Alice: Received OK from Bob, sending Buy to Seller")
        send(cResp.asInstanceOf[OutChannel[Buy|Cancel]], Buy("Street 1, Copenhagen")) >>
        receive(cConf.asInstanceOf[InChannel[Confirm]]) { (conf: Confirm) =>
          println(s"Alice: Purchase confirmed; delivery on ${conf.delivery}")
          nil
      }
      }
    }
  }

  case class Negotiate[V[A] <: RecVar[A]]() extends BobResp {
    type RetTy = Loop[V]
    type ArgTy = V[Unit]
    def ret[ArgTy](x : ArgTy) : RetTy = {
      println(s"Alice: Received Negotiation request from Bob, continue...")
      loop(x.asInstanceOf[V[Unit]])
      }

  }

  case class Buy(address: String)
  case class Confirm(delivery: LocalDate)


  type Seller[CTitle <: InChannel[String],
              CQuote1 <: OutChannel[Quote | NotAvailable],
              CQuote2 <: OutChannel[Quote | NotAvailable],
              CResp <: InChannel[Buy | Cancel],
              CConf <: OutChannel[Confirm]] =


    In[CTitle, String, String =>
        (Out[CQuote1, NotAvailable] >>: Out[CQuote2, NotAvailable])
        |
        TrySell[CQuote1, CQuote2, CResp, CConf]
      ]
  
  type TrySell[CQuote1 <: OutChannel[Quote | NotAvailable],
               CQuote2 <: OutChannel[Quote | NotAvailable],
               CResp <: InChannel[Buy | Cancel],
               CConf <: OutChannel[Confirm]] =
    (Out[CQuote1, Quote] >>: Out[CQuote2, Quote] >>:
      In[CResp, Buy|Cancel, (x: Buy|Cancel) => SellerMatch[x.type, CConf]])
  
  type SellerMatch[X <: Buy|Cancel, CConf <: OutChannel[Confirm]] <: Process = X match {
    case Buy => Out[CConf, Confirm]
    case Cancel => PNil
  }

  type Alice[CTitle <: OutChannel[String],
             CQuote <: InChannel[Quote | NotAvailable],
             CBobContrib <: OutChannel[Contrib|Cancel],
             CBobResp <: InChannel[BobResp],
             CResp <: OutChannel[Buy|Cancel],
             CConf <: InChannel[Confirm]] =
    Out[CTitle, String] >>:
    In[CQuote, Quote|NotAvailable, (x: Quote|NotAvailable) =>
      QuoteMatchA[x.type, CBobContrib, CBobResp, CResp, CConf]
    ]

  type QuoteMatchA[X <: Quote|NotAvailable,
                  CBobContrib <: OutChannel[Contrib|Cancel],
                  CBobResp <: InChannel[BobResp],
                  CResp <: OutChannel[Buy | Cancel],
                  CConf <: InChannel[Confirm]] <: Process = X match {
    case NotAvailable => Out[CBobContrib, Cancel]
    case Quote => Rec[RecX,
      (Out[CBobContrib, Cancel] >>: Out[CResp, Cancel]) |
      (Out[CBobContrib, Contrib] >>:
       In[CBobResp, BobResp, (x: BobResp) => x.RetTy
       ])
    ]
  }

  // This will cause nested matching Type in Alice
  // type BobRespMatch[X <: OK|Cancel|Negotiate,
  //                   CResp <: OutChannel[Buy|Cancel],
  //                   CConf <: InChannel[Confirm]] <: Process = X match {
  //   case Cancel => Out[CResp, Cancel]
  //   case Negotiate => Loop[RecX]
  //   case OK => Out[CResp, Buy] >>: In[CConf, Confirm, Confirm => PNil]
  // }


  //Relaxed type
  // type BobRespMatch[X <: OK|Cancel|Negotiate,
  //                   CResp <: OutChannel[Buy|Cancel],
  //                   CConf <: InChannel[Confirm]] = 
  //   Out[CResp, Cancel] | Loop[RecX] | Out[CResp, Buy] >>: In[CConf, Confirm, Confirm => PNil]


  type Bob[CQuote <: InChannel[Quote | NotAvailable],
            CBobContrib <: InChannel[Contrib|Cancel],
            CBobResp <: OutChannel[BobResp]] =
           In[CQuote, Quote|NotAvailable, (x: Quote|NotAvailable) => QuoteMatchB[x.type, CBobContrib, CBobResp]]

  
      type QuoteMatchB[X <: Quote|NotAvailable,
      CBobContrib <: InChannel[Contrib|Cancel],
      CBobResp <: OutChannel[BobResp],
      ] <: Process = X match {
            case NotAvailable => In[CBobContrib, Contrib |Cancel, (x: Contrib| Cancel) => PNil]
            case Quote =>  Rec[RecY, In[CBobContrib, Contrib |Cancel, (x: Contrib| Cancel) => PNil | Out[CBobResp, Cancel] | Out[CBobResp, OK] | Out[CBobResp, Negotiate[RecVar]] >>: Loop[RecY]]]
            }
}

package implementation {
  import types._
  implicit val timeout: Duration = Duration("30 seconds")
  import effpi.process.dsl._
  var iteration = 0

  def seller(cTitle: InChannel[String],
             cQuote1: OutChannel[Quote | NotAvailable],
             cQuote2: OutChannel[Quote | NotAvailable],
             cResp: InChannel[Buy | Cancel],
             cConf: OutChannel[Confirm]): Seller[cTitle.type, cQuote1.type, cQuote2.type, cResp.type, cConf.type] = {
            println("Seller : starting process")           
            receive(cTitle) {
              case "Croatia" => sell(500, cQuote1, cQuote2, cResp, cConf)
              case "Poland" => sell(700, cQuote1, cQuote2, cResp, cConf)
              case "Netherland" => sell(1000, cQuote1, cQuote2, cResp, cConf)
              case _ => {
                println("Seller : Location not exist, sending not available to alice and bob") 
                send(cQuote1, NotAvailable()) >> send(cQuote2, NotAvailable())
                }
            }
  }

  def sell(amount: Int, cQuote1: OutChannel[Quote|NotAvailable], cQuote2: OutChannel[Quote|NotAvailable],
           cResp: InChannel[Buy | Cancel], cConf: OutChannel[Confirm]): TrySell[cQuote1.type, cQuote2.type, cResp.type, cConf.type] = {

     println(s"Seller : Received request, sending quote ${amount} to Alice") 
     send(cQuote1, Quote(amount)) >> {
      println(s"Seller : Received request, sending quote ${amount} to Bob") 
      send(cQuote2, Quote(amount)) >>
      receive(cResp) { 

        (res: Buy|Cancel) => res match {
        case b: Buy => {
          println(s"Seller: Received Buy request from Alice. Shipping confirmation to: ${b.address}")
          send(cConf, Confirm(LocalDate.now().plusWeeks(1)))
        }
        case _: Cancel => {
          println("Seller : Received cancelled request from Alice, terminating") 
          nil
        }
      } }
    }
  }

  // This does not compile due to https://github.com/lampepfl/dotty/issues/9999

  // Alice's process
  def alice(title: String,
            paying: Int,
            cTitle: OutChannel[String],
            cQuote: InChannel[Quote | NotAvailable],
            cBobContrib: OutChannel[Contrib|Cancel],
            cBobResp: InChannel[BobResp],
            cResp: OutChannel[Buy | Cancel],
            cConf: InChannel[Confirm]): Alice[cTitle.type, cQuote.type, cBobContrib.type, cBobResp.type, cResp.type, cConf.type] = {
     println("Alice : starting process")
     println(s"Alice : sending request to $title")
      send(cTitle, title) >> {
        receive(cQuote) { (q: Quote|NotAvailable) =>
          handleQuoteA(paying, q, cBobContrib, cBobResp, cResp, cConf)
        }
      }
  }
  
  def handleQuoteA(paying: Int, q: Quote|NotAvailable, cBobContrib: OutChannel[Contrib|Cancel],
                  cBobResp: InChannel[BobResp],
                  cResp: OutChannel[Buy | Cancel],
                  cConf: InChannel[Confirm]): QuoteMatchA[q.type, cBobContrib.type,
                                                         cBobResp.type, cResp.type,
                                                         cConf.type] = q match {
        case _: NotAvailable =>{
          println("Alice : Receive Not Available from Seller, sending cancel to Bob")
          send(cBobContrib, Cancel())
        }
        case q: Quote => {
          println(s"Alice : Received quote ${q.amount} from Seller")
          rec(RecX){
            if (q.amount > 1000) {
              println(s"Alice : Starting price ${q.amount} exceeds 1000, send cancel to Bob and seller")
              send(cBobContrib, Cancel()) >> send(cResp, Cancel())
            } else {

            var contrib = 0
            if(iteration <= 7){
              contrib = paying + iteration * 10
            }else{
              println(s"Alice: iteration ${iteration} > 7, price cannot be lower")
              contrib = paying + 7 * 10
            }
              println(s"Alice: sending price ${contrib} to Bob")
              send(cBobContrib, Contrib(contrib)) >>
              receive(cBobResp) { (res: BobResp) => res match{
                case _: Cancel => res.ret(cResp)
                case _: Negotiate[RecVar] => res.ret(RecX)
                case _: OK => res.ret((cResp, cConf))
              }
              }
            }
        }
        }
      }

  
  // def bobRespMatch(res: OK|Cancel|Negotiate,
  //                  cResp: OutChannel[Buy | Cancel],
  //                  cConf: InChannel[Confirm]): BobRespMatch[res.type, cResp.type, cConf.type] = {
  //      res match {
  //         case _: Cancel => res.ret(cResp)
  //           // {
  //           // println(s"Alice: Received Cancel from Bob, sending Cancel to Seller")
  //           // send(cResp, Cancel())
  //           // }
  //         case _: Negotiate => res.ret(RecX)
  //           // {
  //           // println(s"Alice: Received Negotiation request from Bob, continue...")
  //           // loop(RecX)
  //           // }
  //         case _: OK => res.ret((cResp, cConf))
  //         //   {
  //         //   println(s"Alice: Received OK from Bob, sending Buy to Seller")
  //         //   send(cResp, Buy("Street 1, Copenhagen")) >>
  //         //   receive(cConf) { (conf: Confirm) =>
  //         //     println(s"Alice: Purchase confirmed; delivery on ${conf.delivery}")
  //         //     nil
  //         //   }
  //         // }
  //       }
  // }

  def bob(cQuote: InChannel[Quote | NotAvailable],
            cBobContrib: InChannel[Contrib|Cancel],
            cBobResp: OutChannel[BobResp]): Bob[cQuote.type, cBobContrib.type, cBobResp.type] = {
              println("Bob : starting process")
              receive(cQuote) { (q: Quote|NotAvailable) =>
                handleQuoteB(q, cBobContrib, cBobResp)
              }
  }

  def handleQuoteB(q: Quote|NotAvailable, cBobContrib: InChannel[Contrib|Cancel],
                  cBobResp: OutChannel[BobResp]): QuoteMatchB[q.type, cBobContrib.type,
                                                         cBobResp.type] = q match {
        case _: NotAvailable => {
              receive(cBobContrib){
                println("Bob : Received Not Available and Cancel Seller and Alice, Terminating...")
                (res: Contrib| Cancel) => nil
              }
        }
        case q: Quote => rec(RecY) {
          receive(cBobContrib){
            (res: Cancel|Contrib) => res match{
                case _ : Cancel => {
                  println("Bob : Receive Cancel from Alice, Terminating...")
                  nil
                }
                case res : Contrib => {
                  println("Bob : Received Contribution from Alice")
                    if(res.amount < q.amount/2 && iteration < 10){
                       iteration+=1
                       println(s"Bob: Alice paying ${res.amount}, less than half of ${q.amount} and ${iteration - 1} < 10, sending ${iteration}th negotiation")
                       send(cBobResp, Negotiate()) >> loop(RecY)
                    }else if(res.amount >= q.amount/2){
                      println(s"Bob: Alice paying ${res.amount}, more than or equal half of ${q.amount}, sending OK to Alice")
                      iteration = 0
                      send(cBobResp, OK())
                    }else{
                      println(s"Bob: Alice paying ${res.amount}, less than half of ${q.amount} and >= 10 negotiation, sending cancel to Alice")
                       iteration = 0
                       send(cBobResp, Cancel())
                    }
                }
            }
          }
        }
      }

}

// To run this example, try:
// sbt "examples/runMain effpi.examples.demo.twobuyer.Main"
object Main {
  import types._
  import implementation._

  def main(): Unit = main(Array())

  def main(args: Array[String]) = {
    println("Running demo...")
    implicit val ps = effpi.system.ProcessSystemRunnerImproved()
    val destinations = Array(("Luxemborg", 500),("Croatia",300), ("Poland", 290), ("Netherland", 300))
    println()
    for((dest, pay) <- destinations){
      val (ctitle, cquote1, cquote2, cbobcontrib, cbobresp, cresp, cconf) = (Channel[String](), Channel[Quote | NotAvailable](), Channel[Quote | NotAvailable](),
        Channel[Contrib| Cancel](),Channel[BobResp](), Channel[Buy | Cancel](), Channel[Confirm]())
      
      eval(par(seller(ctitle, cquote1, cquote2, cresp, cconf), alice(dest, pay, ctitle, cquote1, cbobcontrib, cbobresp, cresp, cconf),
      bob(cquote2, cbobcontrib, cbobresp)))
      Thread.sleep(3000)
      println()
    }
    ps.kill()
  }
}
