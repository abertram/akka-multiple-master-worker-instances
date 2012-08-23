package sample

import akka.actor.{ActorRef, Props, ActorLogging, Actor}
import akka.util.duration._
import util.Random
import collection.mutable

/**
 * Created by IntelliJ IDEA.
 * User: alex
 * Date: 23.08.12
 * Time: 17:09
 */

class NodeSupervisor extends Actor with ActorLogging {

  import NodeSupervisor._

  val nodeStatistics = mutable.Map[ActorRef, Int]()

  override def preStart() {
    val random = new Random(System.nanoTime)
    log.info("Creating {} nodes", Main.NodeCount)
    val nodes = (1 to Main.NodeCount).map { _ =>
      val node = context.actorOf(Props[Node])
      nodeStatistics += node -> 0
      node
    }
    log.info("{} nodes created", context.children.size)
    log.info("Initializing nodes")
    nodes foreach { node =>
      node ! Node.Init(random.nextInt, nodes.filter(_ != node))
    }
    context.system.scheduler.schedule(1 seconds, 1 seconds, self, ProcessStatistics)
  }

  def processStatistics() {
    log.debug("Workers per node and second: {}", nodeStatistics.map {
      case (node, workersPerSecond) => workersPerSecond
    }.sum / nodeStatistics.size)
  }

  protected def receive = {
    case Node.Statistics(workersPerSecond) =>
      nodeStatistics += sender -> workersPerSecond
    case ProcessStatistics =>
      processStatistics()
  }
}

object NodeSupervisor {

  val ActorName = "node"

  case object ProcessStatistics
}
