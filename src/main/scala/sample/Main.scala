package sample

import akka.actor.{Props, ActorSystem}

/**
 * Created by IntelliJ IDEA.
 * User: alex
 * Date: 23.08.12
 * Time: 12:51
 */

object Main extends App {

  val MaxPathLength = 20
  val NodeCount = 100

  val system = ActorSystem()
  system.log.info("Creating node supervisor")
  system.actorOf(Props[NodeSupervisor], NodeSupervisor.ActorName)
}
