package sample

import akka.actor.{ActorRef, ActorLogging, Actor}
import akka.util.duration._
import collection.mutable
import util.Random

/**
 * Created by IntelliJ IDEA.
 * User: alex
 * Date: 23.08.12
 * Time: 13:13
 */

class Worker extends Actor with ActorLogging {

  import Worker._

  val nodes = mutable.Buffer[ActorRef]()
  val random = new Random(System.nanoTime)
  val processNodeStateDurations = mutable.Buffer[Long]()

  /**
   * Simulates some processing time.
   */
  def count(countLimit: Int) {
    (1 to countLimit).foreach { _ => }
  }

  def nextNode = {
    nodes(random.nextInt(nodes.size))
  }

  def init(nodes: Seq[ActorRef]) {
    this.nodes ++= nodes
    context.system.scheduler.schedule(1 seconds, 1 seconds, self, ProcessStatistics)
    visitNode(nextNode)
  }

  def processNodeState(state: Int) {
//    log.debug("Processing node state")
    val startTime = System.currentTimeMillis
    count(random.nextInt(10e4.toInt))
    val endTime = System.currentTimeMillis - startTime
//    log.debug("Node state processed, took {} milliseconds", endTime)
    processNodeStateDurations += endTime
  }

  def processStatistics() {
    val processNodeStateDuration = if (processNodeStateDurations.size > 0)
      processNodeStateDurations.sum / processNodeStateDurations.size
    else
      0
    context.parent ! Statistics(processNodeStateDuration)
    processNodeStateDurations.clear()
  }

  protected def receive = {
    case Init(nodes) =>
      init(nodes)
    case Node.State(state) =>
      processNodeState(state)
      visitNode(nextNode)
    case ProcessStatistics =>
      processStatistics()
  }

  def visitNode(node: ActorRef) {
//    log.debug("Visiting node")
    node ! Node.Enter
  }
}

object Worker {

  case class Init(nodes: Seq[ActorRef])
  case object ProcessStatistics
  case class Statistics(processTime: Double)
}
