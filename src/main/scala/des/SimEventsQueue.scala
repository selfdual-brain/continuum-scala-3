package com.selfdualbrain.continuum.des

import com.selfdualbrain.continuum.time.SimTimepoint

/**
  * Contract of DES event queue.
  *
  * @tparam A type of agent id
  */
trait SimEventsQueue[A] extends Iterator[Event[A]] {

  /**
    * Adds an event to the timeline.
    */
  def addExternalEvent(timepoint: SimTimepoint, filteringTag: Int, destination: A, payload: AnyRef): Event[A]

  /**
    * Adds an event to the timeline.
    */
  def addTransportEvent(timepoint: SimTimepoint, filteringTag: Int, source: A, destination: A, payload: AnyRef): Event[A]

  /**
    * Adds an event to the timeline.
    */
  def addLoopbackEvent(timepoint: SimTimepoint, filteringTag: Int, agent: A, payload: AnyRef): Event[A]

  /**
    * Adds an event to the timeline.
    */
  def addOutputEvent(timepoint: SimTimepoint, filteringTag: Int, source: A, payload: AnyRef): Event[A]


  def addEngineEvent(timepoint: SimTimepoint, filteringTag: Int, agent: Option[A], payload: AnyRef): Event[A]

  /**
    * Time of last event pulled.
    */
  def currentTime: SimTimepoint

}

case class ExtEventIngredients[A](timepoint: SimTimepoint, filteringTag: Int, destination: A, payload: AnyRef)

/**
  * Base class of (business-logic-independent) event envelopes to be used with SimEventsQueue.
  * @tparam A type of agent identifier
  */
sealed trait Event[A] extends Ordered[Event[A]] {

  def id: Long

  /**
    * Using integer constants as the main dispatching mechanism ensures top-performance in the critical loop of a simulator,
    * while allowing open-ended approach to the set of event types.
    * In a typical simulator there will be several layers using the underlying events queue.
    * We want to keep these layers separate, hence all event types cannot be captured as a single algebraic data type.
    */
  def filteringTag: Int

  def timepoint: SimTimepoint

  override def compare(that: Event[A]): Int = {
    val timeDiff = timepoint.compare(that.timepoint)
    return if (timeDiff != 0)
      timeDiff
    else
      id.compareTo(that.id)
  }

  def loggingAgent: Option[A]

  def payload: AnyRef
}

object Event {

  /**
    * Envelope for "external events".
    * Such events are targeting an agent, but does not have a sender - rather it is the simulation engine itself that sends them.
    * They can be used to represent changes in the environment where agents live.
    *
    * @param id id of this event
    * @param timepoint sim-timepoint when this event should be delivered to the target agent
    * @param destination recipient agent
    * @param payload business-logic-specific payload
    * @tparam A type of agent identifier
    */
  case class External[A](id: Long, filteringTag: Int, timepoint: SimTimepoint, destination: A, payload: AnyRef) extends Event[A] {
    override def loggingAgent: Option[A] = Some(destination)
  }

  /**
    * Envelope for a (agent-to-agent) message-passing events.
    * Such event represents the act of transporting a message from source agent to destination agent.
    * Caution: the timepoint refers to the "delivery" point in time.
    *
    * @param id id of this event
    * @param timepoint sim-timepoint when this event should be delivered to the target agent
    * @param source sending agent
    * @param destination recipient agent
    * @param payload business-logic-specific payload
    * @tparam A type of agent identifier
    * @tparam P type of business-logic-specific payload
    */
  case class Transport[A](id: Long, filteringTag: Int, timepoint: SimTimepoint, source: A, destination: A, payload: AnyRef) extends Event[A] {
    override def loggingAgent: Option[A] = Some(destination)
  }

  /**
    * Envelope for messages scheduled by an agent to itself.
    * Such self-messages can be used for representing async operations and in-agent concurrency.
    * Can be seen as "alerts" or "timers" that an agent sets for itself.
    *
    * @param id id of this event
    * @param timepoint scheduled timepoint of agent "wake up"
    * @param agent agent scheduling this event
    * @param payload business-logic-specific payload
    * @tparam A type of agent identifier
    */
  case class Loopback[A](id: Long, filteringTag: Int, timepoint: SimTimepoint, agent: A, payload: AnyRef) extends Event[A] {
    override def loggingAgent: Option[A] = Some(agent)
  }

  /**
    * Envelope for messages scheduled by the engine.
    * Such messages are not going to be processed by any agent, rather it is the engine itself who schedules them and consumes them.
    * Usage is engine-specific (for example to implement network outage simulation).
    *
    * @param id id of this event
    * @param timepoint scheduled timepoint
    * @param agent relevant agent (if present)
    * @param payload business-logic-specific payload
    * @tparam A type of agent identifier
    */
  case class Engine[A](id: Long, filteringTag: Int, timepoint: SimTimepoint, agent: Option[A], payload: AnyRef) extends Event[A] {
    override def loggingAgent: Option[A] = agent
  }

  /**
    * Envelope for "semantic" events. This is stuff that agents "emits" to the outside world
    * (so, not to be handled by any agent). Just something that an agent wants to announce to whoever is observing the simulation.
    * It can also be seen as structured logging, which is "internally sealed" into the simulated world
    * (not to be mistaken with the logging of the engine that is running the simulation).
    *
    * @param id id of this event
    * @param timepoint sim-timepoint when this event was emitted by source agent
    * @param source reporting agent
    * @param payload business-logic-specific payload
    * @tparam A type of agent identifier
    */
  case class Semantic[A](id: Long, filteringTag: Int, timepoint: SimTimepoint, source: A, payload: AnyRef) extends Event[A] {
    override def loggingAgent: Option[A] = Some(source)
  }

}
