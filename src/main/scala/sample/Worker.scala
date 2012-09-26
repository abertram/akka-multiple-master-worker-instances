package sample

import akka.actor.ActorRef
import util.Random

/**
 * Created by IntelliJ IDEA.
 * User: alex
 * Date: 23.08.12
 * Time: 13:13
 */

class Worker(val destination: ActorRef, val path: Seq[ActorRef]) {

  /**
   * Simulates some processing time.
   */
  def count(countLimit: Int) {
    (1 to countLimit).foreach { _ => }
  }

  def nextNode(node: ActorRef, nodes: Seq[ActorRef], random: Random, state: Int) = {
    // In my real application next node depends on current node and its state. It takes some time to compute next node.
    // This behaviour is simulated by simple counting.
    val time = processNodeState(state, random)
    (new Worker(destination, node +: path), nodes(random.nextInt(nodes.size)), time)
  }

  def processNodeState(state: Int, random: Random) = {
    val startTime = System.currentTimeMillis
    count(random.nextInt(10e4.toInt))
    System.currentTimeMillis - startTime
  }
}

object Worker {

  def apply(destination: ActorRef) = new Worker(destination, Seq())
}
