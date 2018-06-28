package com.hk01.example1

import akka.actor.CoordinatedShutdown.JvmExitReason
import akka.actor.{Actor, ActorLogging, ActorSystem, CoordinatedShutdown, Props}

import scala.concurrent.duration._

object PingPongSystem extends App {
  val system = ActorSystem("pingpong")
  val actorRef = system.actorOf(Props[PongActor], "pong-actor")
  implicit val ec = system.dispatcher
  system.scheduler.schedule(0 seconds, 1 second, actorRef, "ping")

  // shutdown
  scala.io.StdIn.readLine(s"Hit ENTER to exit ...${System.getProperty("line.separator")}")
  println("The world is going to end...")
  CoordinatedShutdown(system).run(JvmExitReason)
}

class PongActor extends Actor with ActorLogging {
  override def receive= {
    case "ping" =>
      log.info("pong")
  }
}

// 車，有咩咁特別
// setInterval(() => console.log("pong"), 1000)
