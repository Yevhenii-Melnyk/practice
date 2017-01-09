package book.kotlin.ch1

import akka.actor.UntypedActor
import akka.event.Logging
import book.kotlin.messages.SetRequest
import book.kotlin.util.system

class AkkademyDb : UntypedActor() {

    val log = Logging.getLogger(context.system, this)
    val map = mutableMapOf<String, Any>()

    override fun onReceive(message: Any) = when (message) {
        is SetRequest -> {
            log.info("received SetRequest - key: {}; value: {}", message.key, message.value)
            map += message.key to message.value
        }
        else -> log.info("received unknown message: {}", message)
    }

}