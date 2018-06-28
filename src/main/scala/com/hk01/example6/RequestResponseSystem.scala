package com.hk01.example6

import akka.actor.CoordinatedShutdown.JvmExitReason
import akka.actor.{Actor, ActorLogging, ActorSystem, CoordinatedShutdown, Props}
import play.api.libs.ws.ahc.StandaloneAhcWSClient

import scala.concurrent.duration._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import play.api.libs.json.JsValue

object RequestResponseSystem extends App {
  val system = ActorSystem("ClientServerSystem")
  implicit val ec = system.dispatcher
  val clientActor =  system.actorOf(Props[ClientActor], "client-actor")

  system.scheduler.scheduleOnce(1 second) {
    clientActor ! "start"
  }

  // shutdown
  scala.io.StdIn.readLine(s"Hit ENTER to exit ...${System.getProperty("line.separator")}")
  println("The world is going to end...")
  CoordinatedShutdown(system).run(JvmExitReason)
}

class ClientActor extends Actor with ActorLogging {

  val timeServerActor = context.actorOf(Props[TimeServerActor], "time-server")
  val apiServerActor = context.actorOf(Props[APIServerActor], "api-server")
  implicit val timeout = Timeout(10 seconds)
  implicit val ec = context.dispatcher

  override def preStart() = {
    log.info("ClientActor preStart...")
  }

  override def postStop() = {
    log.info("ClientActor postStop...")
  }

  override def receive = {
    case "start" =>
      for {
        TimeResponse(timestamp) <- (timeServerActor ? Time).mapTo[TimeResponse]
        APIResponse(intensity, ts) <- (apiServerActor ? API).mapTo[APIResponse]
      } yield {
        log.info(s"timestamp=$timestamp and intensity=$intensity with API complete ts=$ts")
      }
  }
}

class TimeServerActor extends Actor with ActorLogging {

  override def preStart() = {
    log.info("TimeServerActor preStart...")
  }

  override def postStop() = {
    log.info("TimeServerActor postStop...")
  }

  override def receive = {
    case Time =>
      sender() ! TimeResponse(System.currentTimeMillis())
  }
}

class APIServerActor extends Actor with ActorLogging {
  import play.api.libs.ws.JsonBodyReadables._
  implicit val ec = context.dispatcher
  implicit val materializer = ActorMaterializer()
  val wsClient = StandaloneAhcWSClient()

  override def preStart() = {
    log.info("APIServerActor preStart...")
  }

  override def postStop() = {
    log.info("APIServerActor postStop...")
    log.info("closing client...")
    wsClient.close()
  }

  override def receive = {
    case API =>
      log.info("receive API call")
      val replyTo = sender()
      wsClient
        .url("https://api.carbonintensity.org.uk/intensity")
        .get()
        .map { response =>
          val body = response.body[JsValue]
          log.info(s"Got a response $body")
          val intensity = (body \ "data" \ 0 \ "intensity" \ "actual").get.as[Int]
          val now = System.currentTimeMillis()
          replyTo ! APIResponse(intensity, now)
        }
  }
}

sealed trait Request
case object Time extends Request
case object API extends Request

sealed trait Response
case class TimeResponse(timestamp: Long) extends Response
case class APIResponse(intensity: Int, timestamp: Long) extends Response
