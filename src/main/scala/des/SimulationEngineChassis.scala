package com.selfdualbrain.continuum.des
import com.selfdualbrain.continuum.time.{SimTimepoint, TimeDelta}

/**
  * Upgrades any SimulationEngine to ObservableSimulationEngine.
  * Caution: we use Decorator pattern.
  *
  * @param engine underlying simulation engine
  * @tparam A type of agent identifiers
  */
class SimulationEngineChassis[A,P](engine: SimulationEngine[A]) extends ObservableSimulationEngine[A,P] {
  private var observersX: Seq[SimulationObserver[A]] = Seq.empty

  override def addObserver(observer: SimulationObserver[A]): Unit = {
    observersX = observersX.appended(observer)
  }

  override def observers: Iterable[SimulationObserver[A]] = observersX

  override def lastStepEmitted: Long = engine.lastStepEmitted

  override def currentTime: SimTimepoint = engine.currentTime

  override def hasNext: Boolean = engine.hasNext

  override def numberOfAgents: Int = engine.numberOfAgents

  override def agents: Iterable[A] = engine.agents

  override def agentCreationTimepoint(agent: A): SimTimepoint = engine.agentCreationTimepoint(agent)

  override def localClockOfAgent(agent: A): SimTimepoint = engine.localClockOfAgent(agent)

  override def totalConsumedProcessingTimeOfAgent(agent: A): TimeDelta = engine.totalConsumedProcessingTimeOfAgent(agent)

  override def next(): (Long, Event[A]) = {
    val pair = engine.next() //no pattern matching here so to reuse the same tuple instance (= minimize overhead if there is no observers)
    for (observer <- observers)
      observer.onSimulationEvent(pair._1, pair._2)
    return pair
  }

  override def shutdown(): Unit = {
    for (observer <- observers)
      observer.shutdown()
    engine.shutdown()
  }
}
