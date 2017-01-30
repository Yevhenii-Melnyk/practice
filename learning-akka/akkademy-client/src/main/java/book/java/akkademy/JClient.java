package book.java.akkademy;

import akka.actor.ActorSelection;
import akka.actor.ActorSystem;
import book.kotlin.messages.DeleteRequest;
import book.kotlin.messages.GetRequest;
import book.kotlin.messages.SetIfNotExists;
import book.kotlin.messages.SetRequest;

import java.util.concurrent.CompletableFuture;

import static book.kotlin.util.ActorExtensions.ask;


public class JClient {

	private static final int TIMEOUT_MILLIS = 2000;

	private final ActorSystem system = ActorSystem.create("LocalSystem");
	private final ActorSelection remoteDb;

	public JClient(String remoteAddress) {
		remoteDb = system.actorSelection("akka.tcp://akkademy@" + remoteAddress + "/user/akkademy-db");
	}

	public CompletableFuture<String> set(String key, Object value) {
		return ask(remoteDb, new SetRequest(key, value), TIMEOUT_MILLIS);
	}

	public CompletableFuture<String> setIfNotExists(String key, Object value) {
		return ask(remoteDb, new SetIfNotExists(key, value), TIMEOUT_MILLIS);
	}

	public <T> CompletableFuture<T> get(String key) {
		return ask(remoteDb, new GetRequest(key), TIMEOUT_MILLIS);
	}

	public <T> CompletableFuture<T> delete(String key) {
		return ask(remoteDb, new DeleteRequest(key), TIMEOUT_MILLIS);
	}

}
