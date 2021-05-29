package sandbox.caseclass
import java.util.Date

case class Withdraw1(amount:Int)
case class Deposit1(amount:Int)
case class Retry(message:String)
case class Withdraw2(username:String,amount:Int)
case class Deposit2(username:String,amount:Int)
case class Fail(errorCode:Int,errorMessage:String)
case class Cancel()
case class Success(message:String)
case class Login(username:String,pw:String)
case class Continue()
case class Connect()


