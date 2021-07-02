package effpi_sandbox.S.types

import effpi_sandbox.caseclass._
import effpi.process._
import effpi.process.dsl._
import effpi.channel.{InChannel, OutChannel}
import effpi.recurse._

type S[ 
C_S_A_1 <: InChannel[Query],
C_S_A_2 <: OutChannel[Full|Available],
C_S_A_3 <: InChannel[Reject|Confirm]] = 
 Rec[RecS_0, In[C_S_A_1, Query, (x:Query) => ((Out[C_S_A_2,Full] >>: Loop[RecS_0])|(Out[C_S_A_2,Available] >>: In[C_S_A_3, Reject|Confirm, (x:Reject|Confirm) => PNil]))]] 

