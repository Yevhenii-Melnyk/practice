package book.kotlin.util

import akka.actor.*
import akka.testkit.TestActorRef as OriginalTestActorRef

val ActorContext.system: ActorSystem
    get() = system()

fun ActorRef.tell(msg: Any) = tell(msg, ActorRef.noSender())

class TestActorRef {
    companion object {
        inline operator fun <reified T : Actor> invoke(system: ActorSystem): OriginalTestActorRef<T> {
            return OriginalTestActorRef.create(system, Props.create(T::class.java))
        }
    }

}