package book.kotlin.util

import akka.actor.*
import akka.pattern.Patterns
import scala.concurrent.duration.Duration
import akka.actor.Props as OriginalProps
import akka.testkit.TestActorRef as OriginalTestActorRef
import scala.compat.java8.FutureConverters.toJava
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import scala.concurrent.Future as ScalaFuture

val ActorContext.system: ActorSystem
    get() = system()

fun ActorRef.tell(msg: Any) = tell(msg, ActorRef.noSender())

class TestActorRef {

    companion object {
        inline operator fun <reified T : Actor> invoke(system: ActorSystem): OriginalTestActorRef<T> {
            return OriginalTestActorRef.create(system, OriginalProps.create(T::class.java))
        }
    }

}

class Props {

    companion object {
        inline operator fun <reified T : Actor> invoke(): OriginalProps {
            return OriginalProps.create(T::class.java)
        }
    }

}

fun ActorRef.ask(msg: Any, timeoutMillis: Long) = Patterns.ask(this, msg, timeoutMillis)

fun <T> ScalaFuture<Any>.toJava(): CompletableFuture<T> = toJava(this) as CompletableFuture<T>

fun <T> CompletableFuture<T>.block(millis: Long) = this.get(millis, TimeUnit.MILLISECONDS)