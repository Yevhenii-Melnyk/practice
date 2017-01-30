package book.kotlin.ch2

import akka.actor.Status
import book.kotlin.util.KotlinActor

class ReverseStringActor : KotlinActor() {

	override fun onReceive(message: Any) = when (message) {
		is String -> sender.tell(message.reversed())
		else -> sender.tell(Status.Failure(ClassNotFoundException()))
	}

}

