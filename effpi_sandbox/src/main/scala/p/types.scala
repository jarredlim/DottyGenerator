package effpi_sandbox.p.types

import effpi_sandbox.caseclass._
import effpi.process._
import effpi.process.dsl._
import effpi.channel.{InChannel, OutChannel}
import effpi.recurse._

type p[ 
C_p_q_1 <: OutChannel[Data],
C_p_r_1 <: OutChannel[Data]] = 
 Out[C_p_q_1,Data] >>: Out[C_p_r_1,Data] >>: PNil 

