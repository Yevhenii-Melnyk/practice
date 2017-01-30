package book.scala.akkademy

import akka.actor.{Actor, Status}
import akka.event.Logging
import book.scala.messages._

import scala.collection.mutable

class AkkademyDb extends Actor {

	val map = new mutable.HashMap[String, Any]
	val log = Logging(context.system, this)

	override def receive: PartialFunction[Any, Unit] = {
		case SetRequest(key, value) =>
			log.info("received SetRequest - key: {} value: {}", key, value)
			map.put(key, value)
			sender ! Status.Success(key)
		case GetRequest(key) =>
			log.info("received GetRequest - key: {}", key)
			val response: Option[Any] = map.get(key)
			response match {
				case Some(x) => sender ! x
				case None => sender ! Status.Failure(KeyNotFoundException(key))
			}
		case SetIfNotExists(key, value) =>
			log.info("Received SetIfNotExists request – key: {}; value: {}", key, value)
			map.get(key) match {
				case Some(_) => sender ! Status.Failure(KeyAlreadyExistsException(key))
				case None =>
					map += key -> value
					sender ! Status.Success(key)
			}
		case DeleteRequest(key) =>
			log.info("Received DeleteRequest – key: {}", key)
			map.remove(key) match {
				case Some(value) => sender ! Status.Success(value)
				case None => Status.Failure(KeyNotFoundException(key))
			}
		case o =>
			log.info("Received unknown message: {}", o)
			Status.Failure(new ClassNotFoundException)
	}

}
