package sample

import akka.actor.{ActorRef, ActorLogging, Props, Actor}
import akka.util.duration._
import collection.mutable

/**
 * Created by IntelliJ IDEA.
 * User: alex
 * Date: 23.08.12
 * Time: 13:13
 */

class Master extends Actor with ActorLogging {

  import Master._

  val workerStatistics = mutable.Map[ActorRef, Double]()

  override def preStart() {
//    log.info("Creating {} workers", Main.WorkerCount)
    (1 to Main.WorkerCount) foreach { _ =>
      context.actorOf(Props[Worker])
    }
  }

  def init(nodes: Seq[ActorRef]) {
//    log.debug("Initializing")
    context.children foreach { worker =>
      worker ! Worker.Init(nodes)
    }
    context.system.scheduler.schedule(1 seconds, 1 seconds, self, ProcessStatistics)
  }

  def processStatistics() {
    val processNodeStateDuration = if (workerStatistics.size > 0) {
      workerStatistics.map {
        case (ant, processNodeStateDuration) => processNodeStateDuration
      }.sum / workerStatistics.size
    } else {
      0
    }
    context.parent ! Statistics(processNodeStateDuration)
  }

  protected def receive = {
    case Init(nodes) =>
      init(nodes)
    case ProcessStatistics =>
      processStatistics()
    case Worker.Statistics(processNodeStateDuration) =>
      workerStatistics += sender -> processNodeStateDuration
  }
}

object Master {

  val ActorName = "master"

  case class Init(nodes: Seq[ActorRef])
  case object ProcessStatistics
  case class Statistics(processNodeStateDuration: Double)
}
