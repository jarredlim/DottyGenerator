package sandbox.SvrVer.types

import sandbox.caseclass._
import effpi.process._
import effpi.process.dsl._
import effpi.channel.{InChannel, OutChannel}
import effpi.recurse._

type SvrVer_3[ 
X_3 <: Continue|Cancel] <: Process = 
 X_3 match { 
case Continue => Loop[RecSvrVer_4] 
case Cancel => PNil 
} 

type SvrVer_2[ 
X_2 <: Cancel|Continue,
C_SvrVer_Svr_3 <: InChannel[Continue|Cancel]] <: Process = 
 X_2 match { 
case Cancel => PNil 
case Continue => In[C_SvrVer_Svr_3, Continue|Cancel, (X_3:Continue|Cancel) => SvrVer_3[X_3.type]] 
} 

type SvrVer_4[ 
X_4 <: Retry|Cancel] <: Process = 
 X_4 match { 
case Retry => Loop[RecSvrVer_0] 
case Cancel => PNil 
} 

type SvrVer[ 
C_SvrVer_Svr_1 <: InChannel[Login],
C_SvrVer_Svr_2 <: OutChannel[Success|Fail],
C_SvrVer_Svr_3 <: InChannel[Continue|Cancel],
C_SvrVer_Svr_4 <: InChannel[Retry|Cancel]] = 
 Rec[RecSvrVer_0, In[C_SvrVer_Svr_1, Login, (x:Login) => ((Out[C_SvrVer_Svr_2,Success] >>: Rec[RecSvrVer_4, In[C_SvrVer_Svr_3, Cancel|Continue, (X_2:Cancel|Continue) => SvrVer_2[X_2.type,C_SvrVer_Svr_3]]])|(Out[C_SvrVer_Svr_2,Fail] >>: In[C_SvrVer_Svr_4, Retry|Cancel, (X_4:Retry|Cancel) => SvrVer_4[X_4.type]]))]] 

