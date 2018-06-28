package com.hk01.example5

import akka.actor.CoordinatedShutdown.JvmExitReason
import akka.actor.{Actor, ActorLogging, ActorSystem, CoordinatedShutdown, Props}

object RequestResponseSystem extends App {
  val system = ActorSystem("ClientServerSystem")
  implicit val ec = system.dispatcher
}

class ClientActor extends Actor with ActorLogging {
  override def receive = {

  }
}

class ServerActor extends Actor with ActorLogging {
  override def receive = {

  }
}

sealed trait API
case object Time extends API
case 
