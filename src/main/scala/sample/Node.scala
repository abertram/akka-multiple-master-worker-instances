package sample

import akka.actor.{ActorRef, ActorLogging, Actor}
import akka.util.duration._
import collection.mutable
import util.Random
import akka.util.Duration

/**
 * Created by IntelliJ IDEA.
 * User: alex
 * Date: 23.08.12
 * Time: 17:10
 */

class Node extends Actor with ActorLogging {

  import Node._

  val idleTimes = mutable.Buffer[Long]()
  var lastReceiveTime: Option[Long] = None
  val nodes = mutable.Buffer[ActorRef]()
  val random = new Random(System.nanoTime)
  var processedWorkers = 0
  val processStateDurations = mutable.Buffer[Long]()
  val state = random.nextInt

  def init(nodes: Seq[ActorRef]) {
    this.nodes ++= mutable.Buffer(nodes: _*)
    context.system.scheduler.schedule(Duration.Zero, 5 seconds, self, LaunchWorkers)
    context.system.scheduler.schedule(1 seconds, 1 seconds, self, ProcessStatistics)
  }

  def launchWorkers(nodes: Seq[ActorRef]) {
    nodes.foreach { node =>
      val worker = Worker(node)
      val (worker1, nextNode, time) = worker.nextNode(self, nodes.toSeq, random, state)
      nextNode ! worker1
      processStateDurations += time
    }
  }

  def processStatistics() {
    val idleTime = if (idleTimes.size > 0)
      idleTimes.sum.toDouble / idleTimes.size
    else
      0
    val processNodeStateDuration = if (processStateDurations.size > 0)
      processStateDurations.sum.toDouble / processStateDurations.size
    else
      0
    val statistics = Statistics(idleTime, processNodeStateDuration, processedWorkers)
    sendStatistics(statistics)
//    log.debug("{}", statistics)
    resetStatistics()
  }

  def processWorker(worker: Worker) {
    if (worker.destination == self || worker.path.length >= Main.MaxPathLength) {
      // destination or max allowed path length reached
    } else {
      val (worker1, nextNode, time) = worker.nextNode(self, nodes.toSeq, random, state)
      nextNode ! worker1
      processStateDurations += time
    }
  }

  protected def receive = {
    case Init(nodes) =>
      lastReceiveTime.map(idleTimes += System.currentTimeMillis - _)
      lastReceiveTime = Some(System.currentTimeMillis)
      init(nodes)
    case LaunchWorkers =>
      lastReceiveTime.map(idleTimes += System.currentTimeMillis - _)
      lastReceiveTime = Some(System.currentTimeMillis)
      launchWorkers(nodes.toSeq)
    case ProcessStatistics =>
      lastReceiveTime.map(idleTimes += System.currentTimeMillis - _)
      lastReceiveTime = Some(System.currentTimeMillis)
      processStatistics()
    case UpdateState(newState) =>
      lastReceiveTime.map(idleTimes += System.currentTimeMillis - _)
      lastReceiveTime = Some(System.currentTimeMillis)
      // update state
    case worker: Worker =>
      lastReceiveTime.map(idleTimes += System.currentTimeMillis - _)
      lastReceiveTime = Some(System.currentTimeMillis)
      processWorker(worker)
      processedWorkers += 1
  }

  def resetStatistics() {
    idleTimes.clear()
    processedWorkers = 0
    processStateDurations.clear()
  }

  def sendStatistics(statistics: Statistics) {
    context.parent ! statistics
  }
}

object Node {

  case object Enter
  case class Init(nodes: Seq[ActorRef])
  case object LaunchWorkers
  case object ProcessStatistics
  case class Statistics(idleTime: Double, processNodeStateDuration: Double, workersPerSecond: Int)
  case class UpdateState(newState: Int)
}
