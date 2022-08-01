package com.selfdualbrain.continuum.network

import com.selfdualbrain.continuum.des.IntBasedAgentsAddressing
import com.selfdualbrain.continuum.randomness.LongSequence
import com.selfdualbrain.continuum.time.{SimTimepoint, TimeDelta}

import scala.util.Random

/**
  * Idealised model of a network, where:
  * 1. Node connections are represented as explicit graph. For every edge we pick:
  *     - random bandwidth value
  *     - randomly selected gaussian distribution of latencies
  * 2. Additionally, there is a download bandwidth limit defined for each node.
  *
  * Technically our approach is:
  * 1. We assume the same connection speed in both directions (for agents A,B, the speed A->B is the same as the speed B->A).
  * 2. For modeling delays at edge (A,B) in the connection graph we:
  * (a) randomly pick 2 numbers: latency, bandwidth
  * (b) calculate delay for message M as: gaussian(latency, latency * latencyStdDeviationNormalized) + M.size / bandwidth
  */
class SymmetricLatencyBandwidthGraphNetwork[A: IntBasedAgentsAddressing, M: BinarySizeIntrospectionForMessages](
                                             random: Random,
                                             initialNumberOfNodes: Int,
                                             latencyAverageGen: LongSequence.Generator, //here we interpret integers as microseconds
                                             latencyStdDeviationNormalized: Double, //standard deviation of latency expressed as fraction of latency expected value
                                             bandwidthGen: LongSequence.Generator //transfer bandwidth generator connection graph [bits/sec]
                                            ) extends NetworkModel[A,M] {

  case class ConnectionParams(latencyGenerator: LongSequence.Generator, bandwidth: Long)

  private var networkGeometryTable: Array[Array[ConnectionParams]] = Array.ofDim[ConnectionParams](initialNumberOfNodes,initialNumberOfNodes)
  for {
    sourceNode <- 0 until initialNumberOfNodes
    targetNode <- 0 until sourceNode
  } {
    initPair(sourceNode, targetNode)
  }

  private var numberOfNodes: Int = initialNumberOfNodes
  private var growthIncrement: Int = 4

  override def calculateMsgDelay(msg: M, sender: A, destination: A, sendingTime: SimTimepoint): TimeDelta = {
    val senderAddress = implicitly[IntBasedAgentsAddressing[A]].getAddressOf(sender)
    val destinationAddress = implicitly[IntBasedAgentsAddressing[A]].getAddressOf(destination)
    if (senderAddress >= numberOfNodes || destinationAddress >= numberOfNodes) {
      grow(numberOfNodes + growthIncrement)
      growthIncrement = growthIncrement * 2
    }
    val connectionParams = networkGeometryTable(senderAddress)(destinationAddress)
    val latency: TimeDelta = connectionParams.latencyGenerator.next()
    val transferDelay: TimeDelta = implicitly[BinarySizeIntrospectionForMessages[M]].binarySizeOf(msg).toLong * 1000000 / connectionParams.bandwidth
    return latency + transferDelay
  }

  private def initPair(sourceAddress: Int, targetAddress: Int): Unit = {
    val latencyAverage: Long = latencyAverageGen.next()
    val latencyStdDev: Double = latencyAverage * latencyStdDeviationNormalized
    val latencyGen = new LongSequence.Generator.GaussianGen(random, latencyAverage, latencyStdDev)
    val connParams = new ConnectionParams(latencyGen, bandwidthGen.next())
    networkGeometryTable(sourceAddress)(targetAddress) = connParams
    networkGeometryTable(targetAddress)(sourceAddress) = connParams
  }

  private def grow(newNumberOfNodes: Int): Unit = {
    val oldNumberOfNodes = numberOfNodes
    assert (newNumberOfNodes > oldNumberOfNodes)

    //we want to retain geometry of previously existing connections
    val newGeometryTable = Array.ofDim[ConnectionParams](newNumberOfNodes, newNumberOfNodes)
    for {
      sourceNode <- 0 until oldNumberOfNodes
      targetNode <- 0 until oldNumberOfNodes
    } {
      newGeometryTable(sourceNode)(targetNode) = networkGeometryTable(sourceNode)(targetNode)
    }
    networkGeometryTable = newGeometryTable

    //adding definitions of new connections
    for {
      sourceNode <- 0 until newNumberOfNodes
      targetNode <- oldNumberOfNodes until newNumberOfNodes
    } {
      initPair(sourceNode, targetNode)
    }
    numberOfNodes = newNumberOfNodes
  }
}
