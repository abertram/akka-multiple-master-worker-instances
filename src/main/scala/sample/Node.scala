package sample

import akka.actor.{ActorRef, ActorLogging, Actor}
import akka.util.duration._
import collection.mutable
import util.Random

/**
 * Created by IntelliJ IDEA.
 * User: alex
 * Date: 23.08.12
 * Time: 17:10
 */

class Node extends Actor with ActorLogging {

  import Node._

  val nodes = mutable.Buffer[ActorRef]()
  val random = new Random(System.nanoTime)
  var processedWorkers = 0
  val processStateDurations = mutable.Buffer[Long]()
  val state = random.nextInt

  def init(nodes: Seq[ActorRef]) {
    this.nodes ++= mutable.Buffer(nodes: _*)
    context.system.scheduler.schedule(1 seconds, 1 seconds, self, LaunchWorkers)
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
    val processNodeStateDuration = if (processStateDurations.size > 0)
      processStateDurations.sum / processStateDurations.size
    else
      0
    val statistics = Statistics(processNodeStateDuration, processedWorkers)
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
      init(nodes)
    case LaunchWorkers =>
      launchWorkers(nodes.toSeq)
    case ProcessStatistics =>
      processStatistics()
    case UpdateState(newState) =>
      // update state
    case worker: Worker =>
      processWorker(worker)
      processedWorkers += 1
  }

  def resetStatistics() {
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
  case class Statistics(processNodeStateDuration: Double, workersPerSecond: Int)
  case class UpdateState(newState: Int)
}
