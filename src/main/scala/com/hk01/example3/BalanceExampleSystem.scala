package com.hk01.example3

import akka.actor.CoordinatedShutdown.JvmExitReason
import akka.actor.{Actor, ActorLogging, ActorSystem, CoordinatedShutdown, Props}

object BalanceExampleSystem extends App {
  val system = ActorSystem("AccountSystem")
  val accountActor = system.actorOf(Props[AccountBalanceActor], "balance-actor")
  implicit val ec = system.dispatcher

  accountActor ! Deposit(100)
  accountActor ! Withdrawal(100)
  accountActor ! Withdrawal(100)
  accountActor ! Withdrawal(100)
  accountActor ! Withdrawal(100)
  accountActor ! Withdrawal(100)
  accountActor ! Withdrawal(100)
  accountActor ! Withdrawal(100)

  // shutdown
  scala.io.StdIn.readLine(s"Hit ENTER to exit ...${System.getProperty("line.separator")}")
  println("The world is going to end...")
  CoordinatedShutdown(system).run(JvmExitReason)
}

class AccountBalanceActor extends Actor with ActorLogging {
  log.info("init AccountBalance")
  val INIT_BALANCE = 500
  val MIN_BALANCE = 100

  // state
  var balance = INIT_BALANCE

  override def receive = {
    case Deposit(amount) =>
      if (amount > 0) {
        balance += amount
        log.info(s"current balance:$balance")
      } else {
        log.warning("cannot deposit -ve amount")
      }
    case Withdrawal(amount) =>
      // assume we have some business logic to keep balance > MIN_BALANCE after withdrawal
      if (amount > 0 && balance - amount >= MIN_BALANCE) {
        balance -= amount
        log.info(s"current balance:$balance")
      } else {
        log.warning("cannot withdrawal -ve amount")
      }
  }
}

sealed trait AccountCmd
case class Deposit(amount: Int) extends AccountCmd
case class Withdrawal(amount: Int) extends AccountCmd
