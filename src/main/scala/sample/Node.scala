package sample

import akka.actor.{ActorRef, Props, ActorLogging, Actor}
import akka.util.duration._
import collection.mutable

/**
 * Created by IntelliJ IDEA.
 * User: alex
 * Date: 23.08.12
 * Time: 17:10
 */

class Node extends Actor with ActorLogging {

  import Node._

  val masterStatistics = mutable.Map[ActorRef, Double]()
  var workersPerSecond = 0
  var state: Int = _

  context.actorOf(Props[Master], Master.ActorName)

  def init(initialState: Int, nodes: Seq[ActorRef]) {
    state = initialState
    context.actorFor(Master.ActorName) ! Master.Init(nodes)
    context.system.scheduler.schedule(1 seconds, 1 seconds, self, ProcessStatistics)
  }

  def processStatistics() {
    val processNodeStateDuration = if (masterStatistics.size > 0) {
      masterStatistics.map {
        case (ant, processNodeStateDuration) => processNodeStateDuration
      }.sum / masterStatistics.size
    } else {
      0
    }
    val statistics = Statistics(processNodeStateDuration, workersPerSecond)
    sendStatistics(statistics)
//    log.debug("{}", statistics)
    resetStatistics()
  }

  protected def receive = {
    case Enter =>
      sender ! State(state)
      workersPerSecond += 1
    case Init(initialState, nodes) =>
      init(initialState, nodes)
    case Master.Statistics(processNodeStateDuration) =>
      masterStatistics += sender -> processNodeStateDuration
    case ProcessStatistics =>
      processStatistics()
    case UpdateState(newState) =>
      // update state
  }

  def resetStatistics() {
    workersPerSecond = 0
  }

  def sendStatistics(statistics: Statistics) {
    context.parent ! statistics
  }
}

object Node {

  case object Enter
  case class Init(initialState: Int, nodes: Seq[ActorRef])
  case object ProcessStatistics
  case class State(state: Int)
  case class Statistics(processNodeStateDuration: Double, workersPerSecond: Int)
  case class UpdateState(newState: Int)
}
