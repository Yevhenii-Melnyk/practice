package book.scala.ch2

import akka.actor.{Actor, Status}

class ReverseStringActor extends Actor {

	override def receive: Receive = {
		case s: String => sender ! s.reverse
		case _ => sender ! Status.Failure(new ClassNotFoundException())
	}

}
