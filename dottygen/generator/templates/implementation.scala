package effpi_sandbox.ROLE.implementation

import effpi_sandbox.caseclass._
import effpi_sandbox.ROLE.types._
import effpi.process._
import effpi.process.dsl._
import effpi.channel.{InChannel, OutChannel}
import scala.concurrent.duration.Duration
import effpi.recurse._
import java.util.Date

implicit val timeout: Duration = Duration("TIMEOUT seconds")

IMPLEMENTATIONS
