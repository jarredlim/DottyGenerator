package sandbox.caseclass
import java.util.Date

case class OFFER(amount:Int)
case class CONFIRM(departure:Date)
case class PRICE(amount:Int)
case class NEGOTIATE()
case class BUY(address:String)
case class START(location:String)
case class CANCEL()