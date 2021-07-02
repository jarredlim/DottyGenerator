package effpi_sandbox.q.types

import effpi_sandbox.caseclass._
import effpi.process._
import effpi.process.dsl._
import effpi.channel.{InChannel, OutChannel}
import effpi.recurse._

type q_2[ 
X_2 <: Ok|req,
C_q_r_2 <: OutChannel[Ko|Data]] <: Process = 
 X_2 match { 
case Ok => PNil 
case req => ((Out[C_q_r_2,Ko] >>: PNil)|(Out[C_q_r_2,Data] >>: PNil)) 
} 

type q_3[ 
X_3 <: req|Ok,
C_q_r_2 <: OutChannel[Ko|Data]] <: Process = 
 X_3 match { 
case req => ((Out[C_q_r_2,Ko] >>: PNil)|(Out[C_q_r_2,Data] >>: PNil)) 
case Ok => PNil 
} 

type q[ 
C_q_p_1 <: InChannel[Data],
C_q_r_1 <: InChannel[Ok|req],
C_q_r_2 <: OutChannel[Ko|Data]] = 
 InErr[C_q_p_1, Data, (x:Data) => InErr[C_q_r_1, Ok|req, (X_2:Ok|req) => q_2[X_2.type,C_q_r_2], (err:Throwable) => PNil], (err:Throwable) => InErr[C_q_r_1, req|Ok, (X_3:req|Ok) => q_3[X_3.type,C_q_r_2], (err:Throwable) => PNil]] 

