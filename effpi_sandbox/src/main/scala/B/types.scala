package effpi_sandbox.B.types

import effpi_sandbox.caseclass._
import effpi.process._
import effpi.process.dsl._
import effpi.channel.{InChannel, OutChannel}
import effpi.recurse._

type B_2[ 
X_2 <: Full|Quote,
C_B_A_3 <: OutChannel[OK|No]] <: Process = 
 X_2 match { 
case Full => Loop[RecB_0] 
case Quote => ((Out[C_B_A_3,OK] >>: PNil)|(Out[C_B_A_3,No] >>: PNil)) 
} 

type B[ 
C_B_A_1 <: OutChannel[Suggest],
C_B_A_2 <: InChannel[Full|Quote],
C_B_A_3 <: OutChannel[OK|No]] = 
 Rec[RecB_0, Out[C_B_A_1,Suggest] >>: In[C_B_A_2, Full|Quote, (X_2:Full|Quote) => B_2[X_2.type,C_B_A_3]]] 

