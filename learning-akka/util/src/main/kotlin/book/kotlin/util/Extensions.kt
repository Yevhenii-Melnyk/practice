@file:JvmName("ActorExtensions")

package book.kotlin.util

import akka.actor.*
import akka.pattern.Patterns
import scala.compat.java8.FutureConverters.toJava
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.function.Function
import akka.actor.Props as OriginalProps
import akka.testkit.TestActorRef as OriginalTestActorRef
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

fun <T> ActorSelection.ask(msg: Any, timeoutMillis: Long) = Patterns.ask(this, msg, timeoutMillis).toJava<T>()

fun <T> ActorRef.ask(msg: Any, timeoutMillis: Long) = Patterns.ask(this, msg, timeoutMillis).toJava<T>()

fun <T> ActorRef.ask(msg: Any) = Patterns.ask(this, msg, 1000).toJava<T>()

fun <T> ScalaFuture<Any>.toJava(): CompletableFuture<T> = toJava(this).toCompletableFuture() as CompletableFuture<T>

fun <T> CompletableFuture<T>.block(timeoutMillis: Long) = this.get(timeoutMillis, TimeUnit.MILLISECONDS)

fun <T> List<CompletableFuture<T>>.sequence(): CompletableFuture<List<T>> {
	val array = this.toTypedArray()
	val allOf = CompletableFuture.allOf(*array)
	return allOf.thenApply {
		this.map { it.join() }.toList()
	}
}

