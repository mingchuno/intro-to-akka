package com.hk01.example6

import akka.actor.CoordinatedShutdown.JvmExitReason
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, CoordinatedShutdown, Props}

object WorkerMasterSystem extends App {
  val system = ActorSystem("WorkerMasterSystem")
  implicit val ec = system.dispatcher

  val clientActor =  system.actorOf(Props[MasterActor], "master-actor")

  // shutdown
  scala.io.StdIn.readLine(s"Hit ENTER to exit ...${System.getProperty("line.separator")}")
  println("The world is going to end...")
  CoordinatedShutdown(system).run(JvmExitReason)
}

class MasterActor extends Actor with ActorLogging {
  val WORKER_SIZE = 5

  var completeCount = 0
  var totalM = 0
  var totalN = 0

  val actors = Vector.fill(5) {
    val actor = context.actorOf(Props[WorkerActor])
    context watch actor
    actor
  }

  actors.foreach { actor =>
    actor ! WorkRequest(count = 100000)
  }

  override def receive = {
    case "complete" =>
      val pi = 4 * totalN.toFloat / totalM
      log.info(s"work complete and pi is=$pi")
    case WorkResponse(m, n) =>
      log.info(s"1 worker complete with m=$m and n=$n")
      completeCount += 1
      totalM += m
      totalN += n
      if (completeCount == WORKER_SIZE) {
        self ! "complete"
      }
  }
}

class WorkerActor extends Actor with ActorLogging {
  log.info("starting WorkerActor...")
  val r = scala.util.Random
  override def receive: Receive = waitingWork

  override def postStop() = {
    log.info("some worker die...")
  }

  def waitingWork: Receive = {
    case WorkRequest(count) =>
      log.info("receive work request")
      context.become(workMode(sender(),count))
      for (_ <- 1 to count) {
        self ! "work"
      }
      self ! "complete"
  }

  def workMode(replyTo: ActorRef, m: Int, n: Int = 0): Receive = {
    case "work" =>
      val x = r.nextDouble()
      val y = r.nextDouble()
      val insideCircle = Math.sqrt(x*x + y*y) < 1
      if (insideCircle) {
        context.become(workMode(replyTo, m, n + 1))
      }
    case "complete" =>
      replyTo ! WorkResponse(m, n)
  }

}

case class WorkRequest(count: Int)
case class WorkResponse(m: Int, n: Int)


