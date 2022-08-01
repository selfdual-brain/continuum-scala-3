package com.selfdualbrain.continuum.des

sealed abstract class StandardEventPayload {
  val filteringTag: Int
}

object StandardEventPayload {

  case class Halt(reason: String) extends StandardEventPayload {
    override val filteringTag: Int = StandardEventTag.HALT
  }

  case class Heartbeat(impulseNumber: Long) extends StandardEventPayload {
    override val filteringTag: Int = StandardEventTag.HEARTBEAT
  }

  case class Diagnostic(info: String) extends StandardEventPayload {
    override val filteringTag: Int = StandardEventTag.DIAGNOSTIC_INFO
  }

}

object StandardEventTag {
  val HALT = 25
  val DIAGNOSTIC_INFO = 26
  val HEARTBEAT = 27
}