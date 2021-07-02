package effpi_sandbox.A.types

import effpi_sandbox.caseclass._
import effpi.process._
import effpi.process.dsl._
import effpi.channel.{InChannel, OutChannel}
import effpi.recurse._

type A_3[ 
X_3 <: OK|No,
C_A_S_3 <: OutChannel[Confirm],
C_A_S_4 <: OutChannel[Reject]] <: Process = 
 X_3 match { 
case OK => Out[C_A_S_3,Confirm] >>: PNil 
case No => Out[C_A_S_4,Reject] >>: PNil 
} 

type A_2[ 
X_2 <: Available|Full,
C_A_B_2 <: OutChannel[Quote],
C_A_B_4 <: InChannel[OK|No],
C_A_S_3 <: OutChannel[Confirm],
C_A_S_4 <: OutChannel[Reject],
C_A_B_3 <: OutChannel[Full]] <: Process = 
 X_2 match { 
case Available => Out[C_A_B_2,Quote] >>: In[C_A_B_4, OK|No, (X_3:OK|No) => A_3[X_3.type,C_A_S_3,C_A_S_4]] 
case Full => Out[C_A_B_3,Full] >>: Loop[RecA_0] 
} 

type A[ 
C_A_B_1 <: InChannel[Suggest],
C_A_S_1 <: OutChannel[Query],
C_A_S_2 <: InChannel[Available|Full],
C_A_B_2 <: OutChannel[Quote],
C_A_B_4 <: InChannel[OK|No],
C_A_S_3 <: OutChannel[Confirm],
C_A_S_4 <: OutChannel[Reject],
C_A_B_3 <: OutChannel[Full]] = 
 Rec[RecA_0, In[C_A_B_1, Suggest, (x:Suggest) => Out[C_A_S_1,Query] >>: In[C_A_S_2, Available|Full, (X_2:Available|Full) => A_2[X_2.type,C_A_B_2,C_A_B_4,C_A_S_3,C_A_S_4,C_A_B_3]]]] 

