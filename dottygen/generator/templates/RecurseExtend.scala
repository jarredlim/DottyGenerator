package effpi.recurse

import effpi.process._

sealed abstract class RecM[A]() extends RecVar[A]("M")
case object RecM extends RecM[Unit]

