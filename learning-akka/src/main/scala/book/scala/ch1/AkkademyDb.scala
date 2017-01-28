package book.scala.ch1

import akka.actor.{Actor, Status}
import akka.event.Logging
import book.scala.messages.{GetRequest, KeyNotFoundException, SetRequest}

import scala.collection.mutable

class AkkademyDb extends Actor {

	val map = new mutable.HashMap[String, Any]
	val log = Logging(context.system, this)

	override def receive = {
		case SetRequest(key, value) =>
			log.info("received SetRequest - key: {} value: {}", key, value)
			map.put(key, value)
			sender() ! Status.Success
		case GetRequest(key) =>
			log.info("received GetRequest - key: {}", key)
			val response: Option[Any] = map.get(key)
			response match {
				case Some(x) => sender() ! x
				case None => sender() ! Status.Failure(KeyNotFoundException(key))
			}
		case o =>
			log.info("Received unknown message: {}", o)
			Status.Failure(new ClassNotFoundException)
	}

}
