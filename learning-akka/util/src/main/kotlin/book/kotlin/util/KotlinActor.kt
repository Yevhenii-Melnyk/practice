@file:JvmName("KotlinActor")

package book.kotlin.util

import akka.actor.ActorRef
import akka.actor.UntypedActor

abstract class KotlinActor : UntypedActor() {

	fun ActorRef.tell(msg: Any) = tell(msg, self())

}
