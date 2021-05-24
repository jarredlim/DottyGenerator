package sandbox.Client.types

import sandbox.caseclass._
import effpi.process._
import effpi.process.dsl._
import effpi.channel.{InChannel, OutChannel}
import effpi.recurse._

type Client_3[ 
X_3 <: Retry|Success|Cancel] <: Process = 
 X_3 match { 
case Retry => Loop[RecClient_8] 
case Success => Loop[RecClient_8] 
case Cancel => PNil 
} 

type Client_4[ 
X_4 <: Retry|Success|Cancel] <: Process = 
 X_4 match { 
case Retry => Loop[RecClient_8] 
case Success => Loop[RecClient_8] 
case Cancel => PNil 
} 

type Client_2[ 
X_2 <: Retry|Cancel|Success,
C_Client_Svr_4 <: OutChannel[Withdraw1|Deposit1|Cancel],
C_Client_Svr_3 <: InChannel[Retry|Success|Cancel]] <: Process = 
 X_2 match { 
case Retry => Loop[RecClient_1] 
case Cancel => PNil 
case Success => Rec[RecClient_8, ((Out[C_Client_Svr_4,Withdraw1] >>: In[C_Client_Svr_3, Retry|Success|Cancel, (X_3:Retry|Success|Cancel) => Client_3[X_3.type]])|(Out[C_Client_Svr_4,Deposit1] >>: In[C_Client_Svr_3, Retry|Success|Cancel, (X_4:Retry|Success|Cancel) => Client_4[X_4.type]])|(Out[C_Client_Svr_4,Cancel] >>: PNil))] 
} 

type Client[ 
C_Client_Svr_1 <: OutChannel[Connect],
C_Client_Svr_2 <: OutChannel[Login],
C_Client_Svr_4 <: OutChannel[Withdraw1|Deposit1|Cancel],
C_Client_Svr_3 <: InChannel[Retry|Success|Cancel]] = 
 Out[C_Client_Svr_1,Connect] >>: Rec[RecClient_1, Out[C_Client_Svr_2,Login] >>: In[C_Client_Svr_3, Retry|Cancel|Success, (X_2:Retry|Cancel|Success) => Client_2[X_2.type,C_Client_Svr_4,C_Client_Svr_3]]] 

