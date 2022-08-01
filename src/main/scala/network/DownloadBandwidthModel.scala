package com.selfdualbrain.continuum.network

import com.selfdualbrain.continuum.data_structures.FastIntMap
import com.selfdualbrain.continuum.des.IntBasedAgentsAddressing
import com.selfdualbrain.continuum.randomness.LongSequence

trait DownloadBandwidthModel[A] {

  /**
    * Download bandwidth upper limit for specified agent.
    *
    * @param agent agent id
    * @return download bandwidth (as bits/sec)
    */
  def bandwidth(agent: A): Double

}

class UniformBandwidthModel[A](downloadBandwidth: Double) extends DownloadBandwidthModel[A] {
  override def bandwidth(agent: A): Double = downloadBandwidth
}

/**
  * @param bandwidthGen random distribution of bandwidth; we use [bits/sec] units
  */
class GenericBandwidthModel[A: IntBasedAgentsAddressing](initialNumberOfNodes: Int, bandwidthGen: LongSequence.Generator)
  extends DownloadBandwidthModel[A] {

  private val node2downloadBandwidth = new FastIntMap[Double](initialNumberOfNodes)

  override def bandwidth(agent: A): Double = {
    node2downloadBandwidth.get(implicitly[IntBasedAgentsAddressing[A]].getAddressOf(agent)) match {
      case Some(b) => b
      case None =>
        val result = bandwidthGen.next().toDouble
        node2downloadBandwidth(implicitly[IntBasedAgentsAddressing[A]].getAddressOf(agent)) = result
        result
    }
  }

}
