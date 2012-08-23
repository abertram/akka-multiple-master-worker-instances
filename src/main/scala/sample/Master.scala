package sample

import akka.actor.{ActorRef, ActorLogging, Props, Actor}

/**
 * Created by IntelliJ IDEA.
 * User: alex
 * Date: 23.08.12
 * Time: 13:13
 */

class Master extends Actor with ActorLogging {

  import Master._

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
  }

  protected def receive = {
    case Init(nodes) =>
      init(nodes)
  }
}

object Master {

  val ActorName = "master"

  case class Init(nodes: Seq[ActorRef])
}
