package effpi.examples.nestedmatch


/*
package object implementation {
  case class More(); case class Stop();

  sealed abstract class DSL
  case class Fun[F <: More|Stop => DSL](cont: F) extends DSL
  case class Nop() extends DSL

  type Match2[X <: More|Stop] <: DSL = X match {
    case More => Fun[(y: More|Stop) => Match1[y.type]]
    case Stop => Nop
  }
  type Match1[X] <: DSL = X match {
    case More => Nop
    case Stop => Nop
  }

  def fun2(x: More|Stop): Match2[x.type] = x match {
    case _: More => Fun(fun1)
    case _: Stop => Nop()
  }

  def fun1(y: More|Stop): Match1[y.type] = y match {
    case _: More => Nop()
    case _: Stop => Nop()
  }
}
*/

/*
package object test {
  case class More();
  case class Stop();
  type MS = More | Stop

  sealed abstract class DSL
  case class Fun[F <: MS => DSL](cont : F) extends DSL
  case class Nop() extends DSL

  type Match2[X <: MS] <: DSL = X match {
    case More => Fun[(y: MS) => Match1[y.type]]
    case Stop => Nop
  }
  type Match1[X] <: DSL = X match {
    case More => Nop
    case Stop => Nop
  }

  def fun1(y : MS) : Match1[y.type] = y match {
    // 'typed patterns' matches any form of More (which is only More()...)
    case _ : More => Nop()
    case _ : Stop => Nop()
  }

  // def fun2(x : MS) : Match2[x.type] = x match {
  //   case _ : More => Fun(fun1)
  //   case _ : Stop => Nop()
  // }

}

object Main {
  // import implementation._
  // import test._

  def main(): Unit = main(Array()) // if you have no arguments, call main/1
  def main(args: Array[String]) = {
    println("[Main] Started.")
    println("[Main] Finished.")
  }
}
*/

/* implementation._

[info] welcome to sbt 1.3.13 (Ubuntu Java 11.0.9.1)
[info] loading settings for project effpi-adb-build from assembly.sbt,plugins.sbt ...
[info] loading project definition from /home/libellule/Projects/Paxos/effpi-adb/project
[info] loading settings for project effpi from build.sbt ...
[info] set current project to effpi (in build file:/home/libellule/Projects/Paxos/effpi-adb/)
[info] Compiling 2 Scala sources to /home/libellule/Projects/Paxos/effpi-adb/examples/target/scala-3.0.0-M1/classes ...
[error] -- [E043] Type Error: /home/libellule/Projects/Paxos/effpi-adb/examples/src/main/scala/NestedMatch.scala:23:3 
[error] 23 |  }
[error]    |   ^
[error]    |unreducible application of higher-kinded type effpi.examples.nestedmatch.implementation.Match1 to wildcard arguments in subpart effpi.examples.nestedmatch.implementation.Match1[?
[error]    |   <: effpi.examples.nestedmatch.implementation.More | 
[error]    |    effpi.examples.nestedmatch.implementation.Stop
[error]    |] of inferred type 
[error]    |  (x : effpi.examples.nestedmatch.implementation.More | 
[error]    |    effpi.examples.nestedmatch.implementation.Stop
[error]    |  ) match {
[error]    |    case effpi.examples.nestedmatch.implementation.More => 
[error]    |      effpi.examples.nestedmatch.implementation.Fun[(y: 
[error]    |        effpi.examples.nestedmatch.implementation.More
[error]    |       | effpi.examples.nestedmatch.implementation.Stop) => 
[error]    |        effpi.examples.nestedmatch.implementation.Match1[y.type]
[error]    |      ]
[error]    |    case effpi.examples.nestedmatch.implementation.Stop => 
[error]    |      effpi.examples.nestedmatch.implementation.Nop
[error]    |  } <: effpi.examples.nestedmatch.implementation.DSL
[error]    |
[error] one error found
[error] (examples / Compile / compileIncremental) Compilation failed
[error] Total time: 7 s, completed 7 Jan 2021, 18:43:05

*/

/* test._

[info] welcome to sbt 1.3.13 (Ubuntu Java 11.0.9.1)
[info] loading settings for project effpi-adb-build from assembly.sbt,plugins.sbt ...
[info] loading project definition from /home/libellule/Projects/Paxos/effpi-adb/project
[info] loading settings for project effpi from build.sbt ...
[info] set current project to effpi (in build file:/home/libellule/Projects/Paxos/effpi-adb/)
[info] Compiling 2 Scala sources to /home/libellule/Projects/Paxos/effpi-adb/examples/target/scala-3.0.0-M1/classes ...
[warn] Multiple main classes detected.  Run 'show discoveredMainClasses' to see the list
[info] running effpi.examples.nestedmatch.Main 
[Main] Started.
[Main] Finished.
[success] Total time: 9 s, completed 7 Jan 2021, 18:50:46


*/


// /* ----------------------------------------------------------------------------------

// https://scastie.scala-lang.org/FlFrpc7kREmgb1CmLbrKjA

// You can find more examples here:
//   https://github.com/lampepfl/dotty-example-project


abstract class Nat
case class Z() extends Nat
case class S(k : Nat) extends Nat

abstract class Bool
case class TT() extends Bool
case class FF() extends Bool

type A[X <: Option[Integer]] = X match {
  case Nothing => Z
  case Some[Integer] => S
}

def f(x : Option[Integer]) = x match {
  case None => Z()
  case Some(n) => S(Z())
}

type B[X <: Option[Integer], Y <: Bool] <: Bool | Nat = X match {
  case Nothing => Y match {
    case TT => Z
    case FF => FF
  }
  case Some[Integer] => Y
}

/*
def g(x : Option[Integer], y : Bool) : B[x.type, y.type] = x match {
  case None => Z()
  case Some(n) => TT()
}
*/

type C[X <: Nat, Y <: Bool] <: Bool | Nat = X match {
  case Z => D[Y]
  case S => TT
}

type D[Y <: Bool] <: Bool | Nat = Y match {
  case TT => Z
  case FF => TT
}

def h(x : Nat, y : Bool) : C[x.type, y.type] = x match {
  case w1 : Z => y match {
    case z1 : TT => Z()
    case z2 : FF => TT()
  }
  case w2 : S => TT()
}

case class More();
case class Stop();

sealed abstract class DSL
case class Fun[F <: More | Stop => DSL](cont : F) extends DSL
case class Nop() extends DSL

type Match2[X <: More | Stop] <: DSL = X match {
  case More => Fun[(y: More | Stop) => Match1[y.type]]
  case Stop => Nop
}
type Match1[X <: More | Stop] <: DSL = X match {
  case More => Nop
  case Stop => Nop
}

def fun1(y : More | Stop) : Match1[y.type] = y match {
  // 'typed patterns' matches any form of More (which is only More()...)
  case _ : More => Nop()
  case _ : Stop => Nop()
}

/* Unreducible application of higher-kinded type...
def fun2(x : More | Stop) : Match2[x.type] = x match {
  case _ : More => Fun((y : More | Stop) => fun1(y))
  case _ : Stop => Nop()
}
*/

// It doesn't like it because it doesn't know what y is in Match1 (because it's an argument to a lambda).
// This means that when the type checker tries to reduce the function, it can't.
// However, if there's an application which tells us what y is, then surely that would make everybody happy?
// In this case, I think it's trying to reduce it down to a normal form, but can't. It's not clever enough to have an 'or this, or this'.

// ------------------------------------------------------------------------------------------------

// Okay, so it has problems when it encounters a lambda in a type.

/*
// Slimmed down version of TwoBuyer; 
case class Quote(amount : Int)
case class NotAvailable()

case class Cancel()
case class OK()
case class Negotiate()

sealed abstract class Process
case class In[P <: (OK | Cancel | Negotiate) => Process](k : P) extends Process
case class Out() extends Process
case class Loop() extends Process
*/

/* gives us the unreducible application error.
type QuoteMatchA[X <: Quote|NotAvailable] <: Process = X match {
  case NotAvailable => Out
  case Quote =>
    In[(x: OK|Cancel|Negotiate) => BobRespMatch[x.type]]
}

type BobRespMatch[X <: OK | Cancel | Negotiate] <: Process = X match {
  case Cancel => Out
  case Negotiate => Loop
  case OK => Out
}

def bobRespMatch(res: OK|Cancel|Negotiate) : BobRespMatch[res.type] = {
  res match {
    case _: Cancel => Out()
    case _: Negotiate => Loop()
    case _: OK => Out()
  }
}

def handleQuoteA(q : Quote | NotAvailable) : QuoteMatchA[q.type] = {
  q match {
    case _ : NotAvailable => Out()
    // unreducible error arises here
    case q : Quote => In((res : OK | Cancel | Negotiate) => bobRespMatch(res))
  }
}
*/

// The problem is that we need a continuation.
// This is in the receive part. So we don't have the argument at all.
// We want the output to depend upon the input.

// Possibilities:
// Dependent pair, but that would require a proof that it exists, which we don't have.
// Make your own (dependent) lambda type.
// Try unfolding and redefining. Is there a nice equivalence that works?
// Some sort of proof term that states the structure is correct. (As you would in Idris.)
// What if we make the return type part of the definition of the input type itself

// ------------------------------------------------------------------------------------------------

/*
// Unfolding First
type QuoteMatchA[X <: Quote|NotAvailable] <: Process = X match {
  case NotAvailable => Out
  case Quote =>
    In[(x: OK|Cancel|Negotiate) =>
      x.type match {
        case Cancel => Out
        case Negotiate => Loop
        case OK => Out
      }]
}

def handleQuoteA(q : Quote | NotAvailable) : QuoteMatchA[q.type] = {
  q match {
    case _ : NotAvailable => Out()
    // unreducible error arises here
    case q : Quote =>
      // It doesn't seem to be able to unify the below match expression with the type
      //  x.type match { ... } above.
      // It doesn't work for simpler examples, either.
      // I think it has something to do with the way it desugars match type 
      // expressions.
      // So, it's saying that you need to have each nested match in its own 
      // definition.
      In( (res : OK | Cancel | Negotiate) =>
        res match {
          case _: Cancel => Out()
          case _: Negotiate => Loop()
          case _: OK => Out()
        })
  }
}
*/

// ------------------------------------------------------------------------------------------------

/*
// Function call in the type?
type QuoteMatchA[X <: Quote | NotAvailable] <: Process = X match {
  case NotAvailable => Out
  case Quote =>
    In[(x : OK | Cancel | Negotiate) =>
      // nope, can't have a value-level match in a type
      x match {
        case _ : Cancel => Out()
        case _ : Negotiate => Loop()
        case _ : OK => Out()
      }
    ]
}
// Really, 'function call in the type' are what we started with anyway.
*/

// ------------------------------------------------------------------------------------------------

/* I can't express strong enough constraints on arguments of classes/types.
// Our own lambda type.

type Lambda[A, B] = (A, B)

/*
data DPair : (a : Type) -> (P : a -> Type) -> Type where
   MkDPair : {P : a -> Type} -> (x : a) -> P x -> DPair a P
*/

// type DPair[A, B[A]] = (A, (x : A) => B[x.type])

// def mkDPair[A, C, B[C]](x : A, prf : (y : A) => B[y.type]) : DPair[A, (y : A) => B[A]] = (x, prf)

// case class Apply[A, B[A]](prf : (y : A) => B[y.type], x : A, tm : (prf x).type)

type Apply[Prf, X] = Prf(X)
/*
[error] 347 |type Apply[Prf, X] = Prf(X)
[error]     |                     ^^^^^
[error]     |Term-dependent types are experimental,
[error]     |they must be enabled with a `experimental.dependent` language import or setting
*/

case class DPair[A, B[A]](x : A,
                          prf : (y : A) => B[y.type],
                          ok : Apply[prf.type, x.type])
/*
[error] 349 |case class DPair[A, B[A]](x : A,
[error]     |           ^
[error]     |Implementation restriction: case classes cannot have dependencies between parameters
*/

def test(x : Nat) : Lambda[Nat, Bool] = {
  x match {
    case y : Z => (y, TT())
    case z : S => (z, FF())
  }
}
*/

// ------------------------------------------------------------------------------------------------

/* 
trait TyPair {
  type Arg
  type Ret
}

abstract class Lambda[A,B] extends TyPair {
  type Arg = A
  type Ret = B
  type Rel = Arg => Ret
}

// object IsZeroLambda extends Lambda[Nat, Bool] {
  
// }

// Alternatively, our function things extend a trait that defines a corresponding out/consequent type that we use in the continuation.

trait PseudoDepTy {
  type Ret
  // def ret[A](x : A) : Ret
}

case class DIn[P <: Response => Process](k : P) extends Process

sealed abstract class Response extends PseudoDepTy {
  type Ret <: Process
  def ret() : Ret
}

case class DCancel() extends Response {
  type Ret = Out
  def ret() = Out()
}
case class DOK() extends Response {
  type Ret = Out
  def ret() = Out()
}
case class DNegotiate() extends Response {
  type Ret = Loop
  def ret() = Loop()
}

type QuoteMatchA[X <: Quote|NotAvailable] <: Process = X match {
  case NotAvailable => Out
  case Quote => DIn[(x : Response) => x.Ret]
}

def bobRespMatch(res : Response) : res.Ret = res.ret() // {
//   res match {
//     case _ : DCancel => res.ret()
//     case _ : DNegotiate => res.ret()
//     case _ : DOK => res.ret()
//   }
// }

def handleQuoteA(q : Quote | NotAvailable) : QuoteMatchA[q.type] = {
  q match {
    case _ : NotAvailable => Out()
    case q : Quote => DIn((res : Response) => bobRespMatch(res))
  }
}

// Is this the answer?

*/

// ------------------------------------------------------------------------------------------------
// More whole TwoBuyer example

// /*
trait HasRetTy {
  type RetTy
  def ret[ArgTy](x : ArgTy) : RetTy
}

// (Simplified) Process DSL

trait InChannel[+A] { }
trait OutChannel[-A] { }
abstract class ProcVar[A](name : String)
case class ProcX[A]() extends ProcVar[A]("X")
abstract class RecVar[A](name: String) extends ProcVar[A](name)
sealed abstract class RecX[A]() extends RecVar[A]("X")
case object RecX extends RecX[Unit]

sealed abstract class Process
// Omit duration b/c it's uninteresting here.
case class In[C <: InChannel[A], A <: HasRetTy, P <: A => Process](channel : C, cont : P) extends Process
case class Out[C <: OutChannel[A], A](channel: C, v: A) extends Process
case class Call[V[X] <: ProcVar[X], A](procvar: V[A], arg: A) extends Process
type Loop[V[X] <: RecVar[X]] = Call[V, Unit]

sealed abstract class PNil() extends Process
case object nil extends PNil
// Simplified: p1 and p2 originally functions from unit to P1/P2.
case class >>:[P1 <: Process, P2 <: Process](p1: P1, p2: P2) extends Process

// Two Buyer

case class Quote(amount : Int)
case class NotAvailable()
type MaybeQuote = Quote | NotAvailable

case class Contrib(amount : Int)
case class Cancel()
type MaybeContrib = Contrib | Cancel

case class Buy()
type MaybeBuy = Buy | Cancel
// Omitting LocalDate b/c it's uninteresting here.
case class Confirm() extends HasRetTy {
  type RetTy = PNil
  override def ret[Unit](x : Unit) : RetTy = nil
}

// case class OK()
// case class Negotiate()
sealed abstract class BobResp extends HasRetTy {
  type RetTy <: Process
}
case class RCancel() extends BobResp {
  type RetTy = Out[OutChannel[MaybeBuy], Cancel]
  type ArgTy = (OutChannel[MaybeBuy], Cancel)
  override def ret[ArgTy](x : ArgTy) : RetTy = // Out(x._1, x._2)
    x match {
      // case (ch, msg) => Out(ch, msg)
      case (ch, msg) => Out(ch.asInstanceOf[OutChannel[MaybeBuy]], msg.asInstanceOf[Cancel])
    } // Why doesn't the type-checker see that `ch` and `msg` have the type in ArgTy....
      // Note that you can't use (the desugared) Tuple2 because it then complains that '... has not the same kind as its bound [MaybeBuy]'
      // It knows it's a tuple, so why can't you use ._1 and ._2... 
}
case class OK() extends BobResp {
  type RetTy =
    Out[OutChannel[Buy | Cancel], Buy] >>: In[InChannel[Confirm], Confirm, Confirm => PNil]
  type ArgTy = (OutChannel[Buy | Cancel], Buy, InChannel[Confirm], Confirm => PNil)
  // type BuyCh = OutChannel[Buy | Cancel]
  // type CfmCh = InChannel[Confirm]
  // type K = Confirm => PNil
  // override def ret[Tuple4[BuyCh, Buy, CfmCh, K]](x : Tuple4[BuyCh, Buy, CfmCh, K]) : RetTy = 
  override def ret[ArgTy](x : ArgTy) : RetTy = 
    x match {
      case (ch, msg, ch1, k) => // >>:(Out(ch, msg), In(ch1, k))
        >>:(Out(ch.asInstanceOf[OutChannel[Buy | Cancel]], msg.asInstanceOf[Buy]),
            In(ch1.asInstanceOf[InChannel[Confirm]], k.asInstanceOf[Confirm => PNil]))
        // >>:(Out(ch.asInstanceOf[BuyCh], msg.asInstanceOf[Buy]),
        //     In(ch1.asInstanceOf[CfmCh], k.asInstanceOf[K]))
    } 
}
case class Negotiate[V[A] <: RecVar[A]]() extends BobResp {
  type RetTy = Loop[V]
  type ArgTy = V[Unit]
  override def ret[ArgTy](v : ArgTy) : RetTy = Call[V, Unit](v.asInstanceOf[V[Unit]], ())
}

type QuoteMatchA[X <: MaybeQuote,
                 CBobContrib <: OutChannel[Contrib | Cancel],
                 CBobResp <: InChannel[BobResp],
                 CResp <: OutChannel[Buy | Cancel],
                 CConf <: InChannel[Confirm]] <: Process =
  X match {
    case NotAvailable => Out[CBobContrib, Cancel]
    // Simplified this to focus on what's interesting.
    case Quote => In[CBobResp, BobResp, (x : BobResp) => x.RetTy]
  }

def handleQuoteA(q           : MaybeQuote,
                 cBobContrib : OutChannel[Contrib | Cancel],
                 cBobResp    : InChannel[BobResp],
                 cResp       : OutChannel[Buy | Cancel],
                 cConf       : InChannel[Confirm]) :
                QuoteMatchA[q.type, cBobContrib.type, cBobResp.type, cResp.type, cConf.type] = {
    q match {
      case _ : NotAvailable => Out(cBobContrib, Cancel())
      case _ : Quote => In(cBobResp, (res : BobResp) =>
        res match {
          case _ : RCancel => res.ret(())
          case _ : Negotiate[RecVar] => res.ret(RecX)
          case _ : OK => res.ret((cResp, Buy(), cConf, (k : Confirm) => nil))
        })
    }
  }


// Note that this approach is less generic/more restricted than using match types:
// the mapping of T -> S is set, you can't then define T -> U, you need a different T' -> U.

// */


// @main def hello = println(s"${f(None)}")
// @main def hello = println(s"${f(Some(5))}")
// @main def hello = println(s"${g(None, TT())}")
// @main def hello = println(s"${g(Some(5), TT())}")
@main def hello = println(s"${h(Z(), FF())}")


// */
