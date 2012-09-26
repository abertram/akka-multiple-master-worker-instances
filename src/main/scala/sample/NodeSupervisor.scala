package sample

import akka.actor.{ActorRef, Props, ActorLogging, Actor}
import akka.util.duration._
import collection.mutable

/**
 * Created by IntelliJ IDEA.
 * User: alex
 * Date: 23.08.12
 * Time: 17:09
 */

class NodeSupervisor extends Actor with ActorLogging {

  import NodeSupervisor._

  val nodeStatistics = mutable.Map[ActorRef, (Double, Int)]()

  override def preStart() {
    log.info("Creating {} nodes", Main.NodeCount)
    val nodes = (1 to Main.NodeCount).map { _ =>
      val node = context.actorOf(Props[Node])
      nodeStatistics += node -> (0.0, 0)
      node
    }
    log.info("{} nodes created", context.children.size)
    log.info("Initializing nodes")
    nodes foreach { node =>
      node ! Node.Init(nodes.filter(_ != node))
    }
    context.system.scheduler.schedule(1 seconds, 1 seconds, self, ProcessStatistics)
  }

  def processStatistics() {
    log.debug("Processed workers per node and second: {}", nodeStatistics.map {
      case (node, (_, workersPerSecond)) => workersPerSecond
    }.sum / nodeStatistics.size)
    log.debug("Average process node state duration: {} ms", nodeStatistics.map {
      case (node, (processNodeStateDuration, _)) => processNodeStateDuration
    }.sum / nodeStatistics.size)
  }

  protected def receive = {
    case Node.Statistics(processNodeStateDuration, workersPerSecond) =>
      nodeStatistics += sender -> (processNodeStateDuration, workersPerSecond)
    case ProcessStatistics =>
      processStatistics()
  }
}

object NodeSupervisor {

  val ActorName = "node"

  case object ProcessStatistics
}
