package book.kotlin.akkademy

import akka.actor.ActorSelection
import akka.actor.ActorSystem
import book.kotlin.messages.DeleteRequest
import book.kotlin.messages.GetRequest
import book.kotlin.messages.SetIfNotExists
import book.kotlin.messages.SetRequest
import book.kotlin.util.ask
import java.util.concurrent.CompletableFuture

class KClient(remoteAddress: String) {

	companion object {
		private val TIMEOUT_MILLIS = 2000L
	}

	private val system = ActorSystem.create("LocalSystem")
	private val remoteDb: ActorSelection = system.actorSelection("akka.tcp://akkademy@$remoteAddress/user/akkademy-db")

	fun set(key: String, value: Any): CompletableFuture<String> {
		return remoteDb.ask<String>(SetRequest(key, value), TIMEOUT_MILLIS)
	}

	fun setIfNotExists(key: String, value: Any): CompletableFuture<String> {
		return remoteDb.ask<String>(SetIfNotExists(key, value), TIMEOUT_MILLIS)
	}

	fun <T> get(key: String): CompletableFuture<T> {
		return remoteDb.ask<T>(GetRequest(key), TIMEOUT_MILLIS)
	}

	fun <T> delete(key: String): CompletableFuture<T> {
		return remoteDb.ask<T>(DeleteRequest(key), TIMEOUT_MILLIS)
	}

}

