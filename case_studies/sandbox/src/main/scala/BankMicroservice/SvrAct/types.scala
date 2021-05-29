package sandbox.SvrAct.types

import sandbox.caseclass._
import effpi.process._
import effpi.process.dsl._
import effpi.channel.{InChannel, OutChannel}
import effpi.recurse._

type SvrAct_4[ 
X_4 <: Retry|Cancel] <: Process = 
 X_4 match { 
case Retry => Loop[RecSvrAct_6] 
case Cancel => PNil 
} 

type SvrAct_5[ 
X_5 <: Retry|Cancel] <: Process = 
 X_5 match { 
case Retry => Loop[RecSvrAct_6] 
case Cancel => PNil 
} 

type SvrAct_3[ 
X_3 <: Withdraw2|Deposit2|Cancel,
C_SvrAct_Svr_3 <: OutChannel[Success|Fail],
C_SvrAct_Svr_4 <: InChannel[Retry|Cancel]] <: Process = 
 X_3 match { 
case Withdraw2 => ((Out[C_SvrAct_Svr_3,Success] >>: Loop[RecSvrAct_6])|(Out[C_SvrAct_Svr_3,Fail] >>: In[C_SvrAct_Svr_4, Retry|Cancel, (X_4:Retry|Cancel) => SvrAct_4[X_4.type]])) 
case Deposit2 => ((Out[C_SvrAct_Svr_3,Success] >>: Loop[RecSvrAct_6])|(Out[C_SvrAct_Svr_3,Fail] >>: In[C_SvrAct_Svr_4, Retry|Cancel, (X_5:Retry|Cancel) => SvrAct_5[X_5.type]])) 
case Cancel => PNil 
} 

type SvrAct_2[ 
X_2 <: Retry|Cancel|Continue,
C_SvrAct_Svr_2 <: InChannel[Withdraw2|Deposit2|Cancel],
C_SvrAct_Svr_3 <: OutChannel[Success|Fail],
C_SvrAct_Svr_4 <: InChannel[Retry|Cancel]] <: Process = 
 X_2 match { 
case Retry => Loop[RecSvrAct_0] 
case Cancel => PNil 
case Continue => Rec[RecSvrAct_6, In[C_SvrAct_Svr_2, Withdraw2|Deposit2|Cancel, (X_3:Withdraw2|Deposit2|Cancel) => SvrAct_3[X_3.type,C_SvrAct_Svr_3,C_SvrAct_Svr_4]]] 
} 

type SvrAct[ 
C_SvrAct_Svr_1 <: InChannel[Retry|Cancel|Continue],
C_SvrAct_Svr_2 <: InChannel[Withdraw2|Deposit2|Cancel],
C_SvrAct_Svr_3 <: OutChannel[Success|Fail],
C_SvrAct_Svr_4 <: InChannel[Retry|Cancel]] = 
 Rec[RecSvrAct_0, In[C_SvrAct_Svr_1, Retry|Cancel|Continue, (X_2:Retry|Cancel|Continue) => SvrAct_2[X_2.type,C_SvrAct_Svr_2,C_SvrAct_Svr_3,C_SvrAct_Svr_4]]] 

