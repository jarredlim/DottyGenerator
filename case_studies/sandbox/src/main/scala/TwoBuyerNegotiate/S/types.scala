package sandbox.S.types

import sandbox.caseclass._
import effpi.process._
import effpi.process.dsl._
import effpi.channel.{InChannel, OutChannel}
import effpi.recurse._

type S_2[
X2 <: BUY|CANCEL|NEGOTIATE,
C_S_A_5 <: OutChannel[CONFIRM],
C_S_B_3 <: OutChannel[CONFIRM],
C_S_B_4 <: OutChannel[CANCEL],
C_S_B_5 <: OutChannel[NEGOTIATE]] <: Process =
 X2 match {
case BUY => Out[C_S_A_5,CONFIRM] >>: Out[C_S_B_3,CONFIRM] >>: PNil
case CANCEL => Out[C_S_B_4,CANCEL] >>: PNil
case NEGOTIATE => Out[C_S_B_5,NEGOTIATE] >>: Loop[RecS_4]
}

type S[
C_S_A_1 <: InChannel[START],
C_S_A_2 <: OutChannel[PRICE],
C_S_B_1 <: OutChannel[PRICE],
C_S_B_2 <: InChannel[OFFER],
C_S_A_3 <: OutChannel[OFFER],
C_S_A_4 <: InChannel[BUY|CANCEL|NEGOTIATE],
C_S_A_5 <: OutChannel[CONFIRM],
C_S_B_3 <: OutChannel[CONFIRM],
C_S_B_4 <: OutChannel[CANCEL],
C_S_B_5 <: OutChannel[NEGOTIATE]] =
 In[C_S_A_1, START, (x:START) => Out[C_S_A_2,PRICE] >>: Out[C_S_B_1,PRICE] >>: Rec[RecS_4, In[C_S_B_2, OFFER, (x:OFFER) => Out[C_S_A_3,OFFER] >>: In[C_S_A_4, BUY|CANCEL|NEGOTIATE, (x:BUY|CANCEL|NEGOTIATE) => S_2[x.type,C_S_A_5,C_S_B_3,C_S_B_4,C_S_B_5]]]]]