package sandbox.Svr.types

import sandbox.caseclass._
import effpi.process._
import effpi.process.dsl._
import effpi.channel.{InChannel, OutChannel}
import effpi.recurse._

type Svr_4[ 
X_4 <: Success|Fail,
C_Svr_Client_3 <: OutChannel[Success],
C_Svr_SvrVer_3 <: OutChannel[Continue],
C_Svr_Client_4 <: OutChannel[Cancel|Retry],
C_Svr_SvrVer_4 <: OutChannel[Cancel],
C_Svr_SvrAct_8 <: OutChannel[Cancel],
C_Svr_SvrAct_9 <: OutChannel[Retry]] <: Process = 
 X_4 match { 
case Success => Out[C_Svr_Client_3,Success] >>: Out[C_Svr_SvrVer_3,Continue] >>: Loop[RecSvr_8] 
case Fail => ((Out[C_Svr_Client_4,Cancel] >>: Out[C_Svr_SvrVer_4,Cancel] >>: Out[C_Svr_SvrAct_8,Cancel] >>: PNil)|(Out[C_Svr_Client_4,Retry] >>: Out[C_Svr_SvrVer_3,Continue] >>: Out[C_Svr_SvrAct_9,Retry] >>: Loop[RecSvr_8])) 
} 

type Svr_3[ 
X_3 <: Withdraw1|Deposit1|Cancel,
C_Svr_SvrAct_4 <: OutChannel[Withdraw2],
C_Svr_SvrAct_7 <: InChannel[Success|Fail],
C_Svr_Client_3 <: OutChannel[Success],
C_Svr_SvrVer_3 <: OutChannel[Continue],
C_Svr_Client_4 <: OutChannel[Cancel|Retry],
C_Svr_SvrVer_4 <: OutChannel[Cancel],
C_Svr_SvrAct_8 <: OutChannel[Cancel],
C_Svr_SvrAct_9 <: OutChannel[Retry],
C_Svr_SvrAct_5 <: OutChannel[Deposit2],
C_Svr_SvrAct_6 <: OutChannel[Cancel]] <: Process = 
 X_3 match { 
case Withdraw1 => Out[C_Svr_SvrAct_4,Withdraw2] >>: Out[C_Svr_SvrVer_3,Continue] >>: In[C_Svr_SvrAct_7, Success|Fail, (X_4:Success|Fail) => Svr_4[X_4.type,C_Svr_Client_3,C_Svr_SvrVer_3,C_Svr_Client_4,C_Svr_SvrVer_4,C_Svr_SvrAct_8,C_Svr_SvrAct_9]] 
case Deposit1 => Out[C_Svr_SvrAct_5,Deposit2] >>: Out[C_Svr_SvrVer_3,Continue] >>: In[C_Svr_SvrAct_7, Success|Fail, (X_5:Success|Fail) => Svr_4[X_5.type,C_Svr_Client_3,C_Svr_SvrVer_3,C_Svr_Client_4,C_Svr_SvrVer_4,C_Svr_SvrAct_8,C_Svr_SvrAct_9]] 
case Cancel => Out[C_Svr_SvrVer_4,Cancel] >>: Out[C_Svr_SvrAct_6,Cancel] >>: PNil 
} 

type Svr_2[ 
X_2 <: Success|Fail,
C_Svr_SvrAct_1 <: OutChannel[Continue],
C_Svr_Client_5 <: InChannel[Withdraw1|Deposit1|Cancel],
C_Svr_SvrAct_4 <: OutChannel[Withdraw2],
C_Svr_SvrAct_7 <: InChannel[Success|Fail],
C_Svr_Client_3 <: OutChannel[Success],
C_Svr_SvrVer_3 <: OutChannel[Continue],
C_Svr_Client_4 <: OutChannel[Cancel|Retry],
C_Svr_SvrVer_4 <: OutChannel[Cancel],
C_Svr_SvrAct_8 <: OutChannel[Cancel],
C_Svr_SvrAct_9 <: OutChannel[Retry],
C_Svr_SvrAct_5 <: OutChannel[Deposit2],
C_Svr_SvrAct_6 <: OutChannel[Cancel],
C_Svr_SvrVer_5 <: OutChannel[Cancel],
C_Svr_SvrAct_2 <: OutChannel[Cancel],
C_Svr_SvrVer_6 <: OutChannel[Retry],
C_Svr_SvrAct_3 <: OutChannel[Retry]] <: Process = 
 X_2 match { 
case Success => Out[C_Svr_Client_3,Success] >>: Out[C_Svr_SvrAct_1,Continue] >>: Rec[RecSvr_8, In[C_Svr_Client_5, Withdraw1|Deposit1|Cancel, (X_3:Withdraw1|Deposit1|Cancel) => Svr_3[X_3.type,C_Svr_SvrAct_4,C_Svr_SvrAct_7,C_Svr_Client_3,C_Svr_SvrVer_3,C_Svr_Client_4,C_Svr_SvrVer_4,C_Svr_SvrAct_8,C_Svr_SvrAct_9,C_Svr_SvrAct_5,C_Svr_SvrAct_6]]] 
case Fail => ((Out[C_Svr_Client_4,Cancel] >>: Out[C_Svr_SvrVer_5,Cancel] >>: Out[C_Svr_SvrAct_2,Cancel] >>: PNil)|(Out[C_Svr_Client_4,Retry] >>: Out[C_Svr_SvrVer_6,Retry] >>: Out[C_Svr_SvrAct_3,Retry] >>: Loop[RecSvr_1])) 
} 

type Svr[ 
C_Svr_Client_1 <: InChannel[Connect],
C_Svr_Client_2 <: InChannel[Login],
C_Svr_SvrVer_1 <: OutChannel[Login],
C_Svr_SvrVer_2 <: InChannel[Success|Fail],
C_Svr_SvrAct_1 <: OutChannel[Continue],
C_Svr_Client_5 <: InChannel[Withdraw1|Deposit1|Cancel],
C_Svr_SvrAct_4 <: OutChannel[Withdraw2],
C_Svr_SvrAct_7 <: InChannel[Success|Fail],
C_Svr_Client_3 <: OutChannel[Success],
C_Svr_SvrVer_3 <: OutChannel[Continue],
C_Svr_Client_4 <: OutChannel[Cancel|Retry],
C_Svr_SvrVer_4 <: OutChannel[Cancel],
C_Svr_SvrAct_8 <: OutChannel[Cancel],
C_Svr_SvrAct_9 <: OutChannel[Retry],
C_Svr_SvrAct_5 <: OutChannel[Deposit2],
C_Svr_SvrAct_6 <: OutChannel[Cancel],
C_Svr_SvrVer_5 <: OutChannel[Cancel],
C_Svr_SvrAct_2 <: OutChannel[Cancel],
C_Svr_SvrVer_6 <: OutChannel[Retry],
C_Svr_SvrAct_3 <: OutChannel[Retry]] = 
 In[C_Svr_Client_1, Connect, (x:Connect) => Rec[RecSvr_1, In[C_Svr_Client_2, Login, (x:Login) => Out[C_Svr_SvrVer_1,Login] >>: In[C_Svr_SvrVer_2, Success|Fail, (X_2:Success|Fail) => Svr_2[X_2.type,C_Svr_SvrAct_1,C_Svr_Client_5,C_Svr_SvrAct_4,C_Svr_SvrAct_7,C_Svr_Client_3,C_Svr_SvrVer_3,C_Svr_Client_4,C_Svr_SvrVer_4,C_Svr_SvrAct_8,C_Svr_SvrAct_9,C_Svr_SvrAct_5,C_Svr_SvrAct_6,C_Svr_SvrVer_5,C_Svr_SvrAct_2,C_Svr_SvrVer_6,C_Svr_SvrAct_3]]]]] 

