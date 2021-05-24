package sandbox.A.types

import sandbox.caseclass._
import effpi.process._
import effpi.process.dsl._
import effpi.channel.{InChannel, OutChannel}
import effpi.recurse._

type A[
C_A_S_1 <: OutChannel[START],
C_A_S_2 <: InChannel[PRICE],
C_A_S_3 <: InChannel[OFFER],
C_A_S_4 <: OutChannel[NEGOTIATE|BUY|CANCEL],
C_A_S_5 <: InChannel[CONFIRM]] =
 Out[C_A_S_1,START] >>: In[C_A_S_2, PRICE, (x:PRICE) => Rec[RecA_3, In[C_A_S_3, OFFER, (x:OFFER) => ((Out[C_A_S_4,NEGOTIATE] >>: Loop[RecA_3])|(Out[C_A_S_4,BUY] >>: In[C_A_S_5, CONFIRM, (x:CONFIRM) => PNil])|(Out[C_A_S_4,CANCEL] >>: PNil))]]]