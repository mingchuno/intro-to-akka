package com.hk01.example7

import akka.actor.CoordinatedShutdown.JvmExitReason
import akka.actor.{Actor, ActorLogging, ActorSystem, CoordinatedShutdown, Props, Terminated}

import scala.concurrent.duration._
object ErrorHandlingDemoSystem extends App {
  val system = ActorSystem("ErrorHandlingDemoSystem")
  implicit val ec = system.dispatcher

  system.actorOf(Props[MasterActor])

  // shutdown
  scala.io.StdIn.readLine(s"Hit ENTER to exit ...${System.getProperty("line.separator")}")
  println("The world is going to end...")
  CoordinatedShutdown(system).run(JvmExitReason)
}

class MasterActor extends Actor with ActorLogging {
  val tickActor = context.actorOf(Props[TickActor])
  context watch tickActor
  override def receive = {
    case Terminated(`tickActor`) =>
      log.info("TickActor has dead and I know!")
  }
}

class TickActor extends Actor with ActorLogging {
  implicit val ec = context.dispatcher
  context.system.scheduler.schedule(0 seconds, 1 second, self, "tick")
  var count = 0
  def receive = {
    case "tick" =>
      count += 1
      if (count <= 10) {
        log.info(s"Current timestamp = ${System.currentTimeMillis()}")
      } else {
        throw new RuntimeException("simulate some random exception")
      }
  }
}
