package com.selfdualbrain.continuum
package des

trait IntBasedAgentsAddressing[A] {
  def getAddressOf(agent: A): Int
}
