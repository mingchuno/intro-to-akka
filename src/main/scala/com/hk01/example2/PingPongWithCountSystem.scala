package com.hk01.example2

import akka.actor.CoordinatedShutdown.JvmExitReason
import akka.actor.{Actor, ActorLogging, ActorSystem, CoordinatedShutdown, Props}

import scala.concurrent.duration._

object PingPongWithCountSystem extends App {
  val system = ActorSystem("pingpong")
  val actorRef = system.actorOf(Props[PongActorWithCount], "pong-with-count")
  implicit val ec = system.dispatcher
  system.scheduler.schedule(0 seconds, 1 second, actorRef, Ping)

  // shutdown
  scala.io.StdIn.readLine(s"Hit ENTER to exit ...${System.getProperty("line.separator")}")
  println("The world is going to end...")
  CoordinatedShutdown(system).run(JvmExitReason)
}

class PongActorWithCount extends Actor with ActorLogging {
  // mutable here! It is safe to use mutable here!!
  // for those have OCD like me, we can fix it later!!!
  var count = 0
  override def receive = {
    case Ping =>
      count += 1
      log.info(s"my count is $count")
  }
}

sealed trait Data
case object Ping extends Data

// 車，Javascript都得啦
// const getClosure = () => {
//   let count = 0
//   return () => {
//     count += 1
//     console.log(`pong with count ${count}`)
//   }
// }
// setInterval(getClosure(), 1000)
