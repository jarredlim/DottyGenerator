package effpi_sandbox.r.types

import effpi_sandbox.caseclass._
import effpi.process._
import effpi.process.dsl._
import effpi.channel.{InChannel, OutChannel}
import effpi.recurse._

type r[ 
C_r_p_1 <: InChannel[Data],
C_r_q_1 <: OutChannel[Ok],
C_r_q_2 <: OutChannel[req],
C_r_q_3 <: InChannel[Ko|Data]] = 
 InErr[C_r_p_1, Data, (x:Data) => Out[C_r_q_1,Ok] >>: PNil, (err:Throwable) => Out[C_r_q_2,req] >>: InErr[C_r_q_3, Ko|Data, (x:Ko|Data) => PNil, (err:Throwable) => PNil]] 

