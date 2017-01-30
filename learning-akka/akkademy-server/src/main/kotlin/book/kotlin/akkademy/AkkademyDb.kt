package book.kotlin.akkademy

import akka.actor.Status
import akka.event.Logging
import book.kotlin.messages.*
import book.kotlin.util.KotlinActor
import book.kotlin.util.system

class AkkademyDb : KotlinActor() {

	val log = Logging.getLogger(context.system, this)
	val map = mutableMapOf<String, Any>()

	override fun onReceive(message: Any) = when (message) {
		is SetRequest -> {
			log.info("received SetRequest - key: {}; value: {}", message.key, message.value)
			map += message.key to message.value
			sender.tell(Status.Success(message.key))
		}
		is GetRequest -> {
			log.info("Received get request – key: {}", message.key)
			val value = map[message.key]
			val response = value ?: Status.Failure(KeyNotFoundException(message.key))
			sender.tell(response)
		}
		is SetIfNotExists -> {
			log.info("Received SetIfNotExists request – key: {}; value: {}", message.key, message.value)
			val previousValue = map.putIfAbsent(message.key, message.value)
			val response: Status.Status =
					if (previousValue == null) Status.Success(message.key)
					else Status.Failure(KeyAlreadyExistsException(message.key))
			sender.tell(response)
		}
		is DeleteRequest -> {
			log.info("Received DeleteRequest – key: {}", message.key)
			val removed = map.remove(message.key)
			val response: Status.Status =
					if (removed != null) Status.Success(removed)
					else Status.Failure(KeyNotFoundException(message.key))
			sender.tell(response)
		}
		else -> {
			log.info("received unknown message: {}", message)
			sender.tell(Status.Failure(ClassNotFoundException(message::class.toString())))
		}
	}

}
