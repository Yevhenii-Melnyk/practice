package book.scala.ch2

import akka.actor.{Actor, Status}

class ScalaPongActor extends Actor {
	override def receive: Receive = {
		case "Ping" => sender ! "Pong"
		case _ => sender ! Status.Failure(new Exception("unknown message"))
	}
}