package com.selfdualbrain.continuum.des

/**
  * Simulation engine with added feature of "observers".
  * Any number of observers can subscribe to the engine. As the events are pulled from the engine,
  * every observer will be notified.
  *
  * @tparam A type of agent identifiers
  */
trait ObservableSimulationEngine[A,P] extends SimulationEngine[A] {

  def addObserver(observer: SimulationObserver[A]): Unit

  def observers: Iterable[SimulationObserver[A]]

}

/**
  * A contract of simulation observer.
  * Such an observer can be plugged into ObservableSimulationEngine to be notified about subsequent events emitted by the simulation engine.
  *
  * @tparam A type of agent identifiers
  */
trait SimulationObserver[A] {

  /**
    * Yet another simulation event has just been emitted by the engine.
    * Do any processing needed.
    *
    * @param step number of simulation "step"
    * @param event event data
    */
  def onSimulationEvent(step: Long, event: Event[A]): Unit

  /**
    * Gives a chance to release resources allocated by this observer.
    */
  def shutdown(): Unit = {
    //by default do nothing
  }

}
