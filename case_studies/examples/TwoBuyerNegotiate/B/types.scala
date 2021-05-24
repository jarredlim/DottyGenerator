package sandbox.B.types

import sandbox.caseclass._
import effpi.process._
import effpi.process.dsl._
import effpi.channel.{InChannel, OutChannel}
import effpi.recurse._

type B_2[
X2 <: NEGOTIATE|CONFIRM|CANCEL] <: Process =
 X2 match {
case NEGOTIATE => Loop[RecB_2]
case CONFIRM => PNil
case CANCEL => PNil
}

type B[
C_B_S_1 <: InChannel[PRICE],
C_B_S_2 <: OutChannel[OFFER],
C_B_S_3 <: InChannel[NEGOTIATE|CONFIRM|CANCEL]] =
 In[C_B_S_1, PRICE, (x:PRICE) => Rec[RecB_2, Out[C_B_S_2,OFFER] >>: In[C_B_S_3, NEGOTIATE|CONFIRM|CANCEL, (x:NEGOTIATE|CONFIRM|CANCEL) => B_2[x.type]]]]