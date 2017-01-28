package book.kotlin.ch2

import akka.actor.Status
import akka.actor.UntypedActor
import book.kotlin.util.tell

class KotlinPongActor : UntypedActor() {

    override fun onReceive(message: Any) {
        println(message)
        when (message) {
            "Ping" -> sender.tell("Pong")
            else -> sender.tell(Status.Failure(Exception("unknown message")), self)
        }
    }

}